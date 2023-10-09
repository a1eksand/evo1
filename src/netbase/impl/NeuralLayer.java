package netbase.impl;

import java.io.Serializable;
import netbase.ActivationFunction;

public abstract class NeuralLayer implements Serializable {

  /**
   * Number of Neurons in this Layer
   */
  protected int numberOfNeuronsInLayer;
  /**
   * Activation Function of this Layer
   */
  protected ActivationFunction activationFnc;
  /**
   * Previous Layer that feeds values to this Layer
   */
  protected NeuralLayer previousLayer;
  /**
   * Next Layer which this Layer will feed values to
   */
  protected NeuralLayer nextLayer;
  /**
   * Array of input values that are fed to this Layer
   */
  protected double[] inputs;
  /**
   * Array of output values this Layer will produce
   */
  protected double[] outputs;
  /**
   * Number of Inputs this Layer can receive
   */
  protected int numberOfInputs;
  /**
   * Array of Neurons of this Layer
   */
  protected Neuron[] neurons;

  /**
   * NeuralLayer constructor
   *
   * @param numberofneurons Number of Neurons in this Layer
   * @param iaf Activation Function for all neurons in this Layer
   * @see NeuralLayer
   */
  public NeuralLayer(int numberofneurons, ActivationFunction iaf) {
    this.numberOfNeuronsInLayer = numberofneurons;
    this.activationFnc = iaf;
    neurons = new Neuron[numberofneurons];
    outputs = new double[numberofneurons];
  }

  /**
   * getNumberOfNeuronsInLayer
   *
   * @return Returns the number of neurons in this layer
   */
  public int getNumberOfNeuronsInLayer() {
    return numberOfNeuronsInLayer;
  }

  /**
   * getListOfNeurons
   *
   * @return Returns the whole array of neurons of this layer
   */
  public Neuron[] getNeurons() {
    return neurons;
  }

  /**
   * getPreviousLayer
   *
   * @return Returns the reference to the previous layer
   */
  protected NeuralLayer getPreviousLayer() {
    return previousLayer;
  }

  /**
   * setPreviousLayer
   *
   * @param layer Sets the reference to the previous layer
   */
  protected void setPreviousLayer(NeuralLayer layer) {
    previousLayer = layer;
  }

  /**
   * getNextLayer
   *
   * @return Returns the reference to the next layer
   */
  protected NeuralLayer getNextLayer() {
    return nextLayer;
  }

  /**
   * setNextLayer
   *
   * @param layer Sets the reference to the next layer
   */
  protected void setNextLayer(NeuralLayer layer) {
    nextLayer = layer;
  }

  /**
   * init
   * Initializes the Neural Layer by setting the activation function for all
   * neurons of this layer and then initializing each neuron.
   *
   * @see NeuralLayer
   */
  protected void init(DoubleIterator doubleIterator) {
    if (numberOfNeuronsInLayer >= 0) {
      for (int i = 0; i < numberOfNeuronsInLayer; i++) {
        var neuron = new Neuron(numberOfInputs, activationFnc);
        neuron.init(doubleIterator);
        neurons[i] = neuron;
      }
    }
  }

  /**
   * setInputs
   * Sets an array of real values to this layer's input
   *
   * @param inputs array of real values to be fed into this layer's input
   * @see NeuralInput
   */
  protected void setInputs(double[] inputs) {
    assert this.numberOfInputs == inputs.length;
    this.inputs = inputs;
  }

  /**
   * calc
   * Calculates the outputs of all neurons of this layer
   */
  protected void calc() {
    for (int i = 0; i < numberOfNeuronsInLayer; i++) {
      outputs[i] = neurons[i].calc(this.inputs);
    }
  }

  /**
   * getOutputs
   *
   * @return Returns the array of this layer's outputs
   */
  protected double[] getOutputs() {
    return outputs;
  }
}
