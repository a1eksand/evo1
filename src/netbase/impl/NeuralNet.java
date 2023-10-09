package netbase.impl;

import java.io.Serializable;
import netbase.ActivationFunction;

public class NeuralNet implements Serializable {

  /**
   * Neural Network Input Layer
   */
  private InputLayer inputLayer;
  /**
   * Neural Network array of hidden layers, that may contain 0 or many
   */
  private HiddenLayer[] hiddenLayer;
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
   * NeuralNet constructor
   * This constructor initializes the neural network by initializing all of
   * the underlying layers and their respective neurons.
   *
   * @param numberofinputs Number of Inputs of this Neural Network
   * @param numberofoutputs Number of Outputs of this Neural Network
   * @param numberofhiddenneurons Array containing the number of Neurons in
   * each of the Hidden Layers
   * @param hiddenAcFnc Array containing the activation function of each
   * Hidden Layer
   * @param outputAcFnc Activation Function of the Output Layer
   */
  public NeuralNet(
      int numberofinputs, int numberofoutputs,
      int[] numberofhiddenneurons, ActivationFunction[] hiddenAcFnc,
      ActivationFunction outputAcFnc, DoubleIterator doubleIterator
  ) {
    numberOfHiddenLayers = numberofhiddenneurons.length;
    numberOfInputs = numberofinputs;
    if (numberOfHiddenLayers == hiddenAcFnc.length) {
      inputLayer = new InputLayer(numberofinputs, doubleIterator);
      if (numberOfHiddenLayers > 0) {
        hiddenLayer = new HiddenLayer[numberOfHiddenLayers];
      }
      for (int i = 0; i < numberOfHiddenLayers; i++) {
        if (i == 0) {
          hiddenLayer[i] = new HiddenLayer(numberofhiddenneurons[i], hiddenAcFnc[i], inputLayer.getNumberOfNeuronsInLayer(), doubleIterator);
          inputLayer.setNextLayer(hiddenLayer[i]);
        } else {
          hiddenLayer[i] = new HiddenLayer(numberofhiddenneurons[i], hiddenAcFnc[i], hiddenLayer[i - 1].getNumberOfNeuronsInLayer(), doubleIterator);
          hiddenLayer[i - 1].setNextLayer(hiddenLayer[i]);
        }
      }
      if (numberOfHiddenLayers > 0) {
        outputLayer = new OutputLayer(
            numberofoutputs,
            outputAcFnc,
            hiddenLayer[numberOfHiddenLayers - 1].getNumberOfNeuronsInLayer(),
            doubleIterator
        );
        hiddenLayer[numberOfHiddenLayers - 1].setNextLayer(outputLayer);
      } else {
        outputLayer = new OutputLayer(numberofinputs, outputAcFnc, numberofoutputs, doubleIterator);
        inputLayer.setNextLayer(outputLayer);
      }
    }
  }


  /**
   * calc
   * This method calculates the output of each layer and forwards all values
   * to the next layer
   */
  public double[] calc(double[] inputs) {
    assert this.numberOfInputs == inputs.length;
    inputLayer.setInputs(inputs);
    inputLayer.calc();
    if (numberOfHiddenLayers > 0) {
      for (int i = 0; i < numberOfHiddenLayers; i++) {
        HiddenLayer hl = hiddenLayer[i];
        hl.setInputs(hl.getPreviousLayer().getOutputs());
        hl.calc();
      }
    }
    outputLayer.setInputs(outputLayer.getPreviousLayer().getOutputs());
    outputLayer.calc();
    return outputLayer.getOutputs();
  }
}
