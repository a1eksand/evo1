package netbase.impl;

import java.io.Serializable;
import java.util.ArrayList;
import netbase.ActivationFunction;

public class NeuralNet implements Serializable {

  /**
   * Neural Network Input Layer
   */
  private InputLayer inputLayer;
  /**
   * Neural Network array of hidden layers, that may contain 0 or many
   */
  private ArrayList<HiddenLayer> hiddenLayer;
  /**
   * Neural Network Output Layer
   */
  private OutputLayer outputLayer;

  /**
   * Number of Hidden Layers
   */
  private int numberOfHiddenLayers;
  /**
   * Number of Inputs
   */
  private int numberOfInputs;
  /**
   * Number of Outputs
   */
  private int numberOfOutputs;

  /**
   * Array of neural inputs
   */
  private Doubles input;
  /**
   * Array of neural outputs
   */
  private Doubles output;

  /**
   * NeuralNet constructor
   * This constructor initializes the neural network by initializing all of
   * the underlying layers and their respective neurons.
   *
   * @param numberofinputs        Number of Inputs of this Neural Network
   * @param numberofoutputs       Number of Outputs of this Neural Network
   * @param numberofhiddenneurons Array containing the number of Neurons in
   *                              each of the Hidden Layers
   * @param hiddenAcFnc           Array containing the activation function of each
   *                              Hidden Layer
   * @param outputAcFnc           Activation Function of the Output Layer
   */
  public NeuralNet(
      int numberofinputs, int numberofoutputs,
      int[] numberofhiddenneurons, ActivationFunction[] hiddenAcFnc,
      ActivationFunction outputAcFnc, DoubleIterator doubleIterator
  ) {
    numberOfHiddenLayers = numberofhiddenneurons.length;
    numberOfInputs = numberofinputs;
    numberOfOutputs = numberofoutputs;
    if (numberOfHiddenLayers == hiddenAcFnc.length) {
      input = new Doubles(numberofinputs);
      inputLayer = new InputLayer(numberofinputs, doubleIterator);
      if (numberOfHiddenLayers > 0) {
        hiddenLayer = new ArrayList<>(numberOfHiddenLayers);
      }
      for (int i = 0; i < numberOfHiddenLayers; i++) {
        if (i == 0) {
          try {
            hiddenLayer.set(i, new HiddenLayer(numberofhiddenneurons[i],
                hiddenAcFnc[i],
                inputLayer.getNumberOfNeuronsInLayer(), doubleIterator
            ));
          } catch (IndexOutOfBoundsException iobe) {
            hiddenLayer.add(new HiddenLayer(numberofhiddenneurons[i],
                hiddenAcFnc[i],
                inputLayer.getNumberOfNeuronsInLayer(), doubleIterator
            ));
          }
          inputLayer.setNextLayer(hiddenLayer.get(i));
        } else {
          try {
            hiddenLayer.set(i, new HiddenLayer(numberofhiddenneurons[i],
                hiddenAcFnc[i], hiddenLayer.get(i - 1)
                .getNumberOfNeuronsInLayer(), doubleIterator
            ));
          } catch (IndexOutOfBoundsException iobe) {
            hiddenLayer.add(new HiddenLayer(numberofhiddenneurons[i],
                hiddenAcFnc[i], hiddenLayer.get(i - 1)
                .getNumberOfNeuronsInLayer(), doubleIterator
            ));
          }
          hiddenLayer.get(i - 1).setNextLayer(hiddenLayer.get(i));
        }
      }
      if (numberOfHiddenLayers > 0) {
        outputLayer = new OutputLayer(numberofoutputs, outputAcFnc,
            hiddenLayer.get(numberOfHiddenLayers - 1)
                .getNumberOfNeuronsInLayer(), doubleIterator
        );
        hiddenLayer.get(numberOfHiddenLayers - 1).setNextLayer(outputLayer);
      } else {
        outputLayer = new OutputLayer(numberofinputs, outputAcFnc,
            numberofoutputs, doubleIterator
        );
        inputLayer.setNextLayer(outputLayer);
      }
    }
  }


  /**
   * setInputs
   * Sets a vector of double-precision values into the neural network inputs
   *
   * @param inputs vector of values to be fed into the neural inputs
   */
  public void setInputs(double[] inputs) {
    if (inputs.length == numberOfInputs) {
      input.set(inputs);
    } else {
      throw new IndexOutOfBoundsException();
    }
  }

  /**
   * calc
   * This method calculates the output of each layer and forwards all values
   * to the next layer
   */
  public void calc() {
    inputLayer.setInputs(input);
    inputLayer.calc();
    if (numberOfHiddenLayers > 0) {
      for (int i = 0; i < numberOfHiddenLayers; i++) {
        HiddenLayer hl = hiddenLayer.get(i);
        hl.setInputs(hl.getPreviousLayer().getOutputs());
        hl.calc();
      }
    }
    outputLayer.setInputs(outputLayer.getPreviousLayer().getOutputs());
    outputLayer.calc();
    this.output = outputLayer.getOutputs();
  }

  /**
   * getArrayOutputs
   *
   * @return Returns the neural outputs in the form of Array
   */
  public Doubles getOutputs() {
    return output;
  }
}
