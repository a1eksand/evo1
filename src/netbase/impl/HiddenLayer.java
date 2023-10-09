package netbase.impl;

import netbase.ActivationFunction;

public class HiddenLayer extends NeuralLayer {

  /**
   * HiddenLayer constructor
   *
   * @param numberofneurons Number of neurons in this hidden layer
   * @param iaf Activation Function for all neurons in this layer
   * @param numberofinputs Number of inputs in this layer
   * @see HiddenLayer
   */
  public HiddenLayer(
      int numberofneurons, ActivationFunction iaf,
      int numberofinputs, DoubleIterator doubleIterator
  ) {
    super(numberofneurons, iaf);
    numberOfInputs = numberofinputs;
    init(doubleIterator);
  }

  /**
   * setPreviousLayer
   * This method links this layer to a previous layer in the Neural Network
   *
   * @param previous Previous Neural Layer
   * @see HiddenLayer
   */
  @Override
  public void setPreviousLayer(NeuralLayer previous) {
    this.previousLayer = previous;
    if (previous.nextLayer != this) {
      previous.setNextLayer(this);
    }
  }

  /**
   * setNextLayer
   * This method links this layer to a next layer in the Neural Network
   *
   * @param next Next Neural Layer
   * @see HiddenLayer
   */
  @Override
  public void setNextLayer(NeuralLayer next) {
    nextLayer = next;
    if (next.previousLayer != this) {
      next.setPreviousLayer(this);
    }
  }

}
