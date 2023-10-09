package netbase.impl;

import netbase.Linear;

public class InputLayer extends NeuralLayer {

  /**
   * InputLayer constructor
   *
   * @param numberofinputs Number of Inputs of this layer and the Neural
   * Network
   * @see InputLayer
   */
  public InputLayer(int numberofinputs, DoubleIterator doubleIterator) {
    super(numberofinputs, new Linear(1));
    previousLayer = null;
    numberOfInputs = numberofinputs;
    init(doubleIterator);
  }

  /**
   * setNextLayer
   * This method links this layer to a next layer in the Neural Network
   *
   * @param layer Next Neural Layer
   * @see InputLayer
   */
  @Override
  public void setNextLayer(NeuralLayer layer) {
    nextLayer = layer;
    if (layer.previousLayer != this) {
      layer.setPreviousLayer(this);
    }
  }

  /**
   * setPreviousLayer
   * This method prevents any attempt to link this layer to a previous one,
   * provided that this should be the first layer
   *
   * @param layer dummy Neural Layer
   * @see InputLayer
   */
  @Override
  public void setPreviousLayer(NeuralLayer layer) {
    previousLayer = null;
  }

  /**
   * init
   * This method initializes all neurons of this layer
   *
   * @see InputLayer
   */
  @Override
  public void init(DoubleIterator doubleIterator) {
    for (int i = 0; i < numberOfInputs; i++) {
      neurons[i] = new InputNeuron();
      neurons[i].init(doubleIterator);
    }
  }

  /**
   * setInputs
   * This method feeds an array of real values into this layer's inputs
   *
   * @param inputs array of values to be fed into the layer's inputs
   * @see InputLayer
   */
  @Override
  public void setInputs(double[] inputs) {
    assert this.numberOfInputs == inputs.length;
    this.inputs = inputs;
  }

  /**
   * calc
   * This method overrides the superclass calc because it just passes the
   * input values to the outputs, provided this is the input layer
   *
   * @see InputLayer
   */
  @Override
  public void calc() {
    for (int i = 0; i < numberOfNeuronsInLayer; i++) {
      double[] firstInput = {this.inputs[i]};
      outputs[i] = neurons[i].calc(firstInput);
    }
  }
}
