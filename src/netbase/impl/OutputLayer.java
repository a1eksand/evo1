package netbase.impl;

import netbase.ActivationFunction;

public class OutputLayer extends NeuralLayer {

  /**
   * OutputLayer constructor
   *
   * @param numberofneurons Number of Neurons (and also the outputs) of this
   * layer
   * @param iaf Activation Function of this layer
   * @param numberofinputs Number of Inputs of this layer
   */
  public OutputLayer(int numberofneurons, ActivationFunction iaf, int numberofinputs, DoubleIterator doubleIterator) {
    super(numberofneurons, iaf);
    numberOfInputs = numberofinputs;
    nextLayer = null;
    init(doubleIterator);
  }

  /**
   * setNextLayer
   * This method prevents any attempt to link this layer to a next one,
   * provided that this should be always the last
   *
   * @param layer Dummy layer
   */
  @Override
  public void setNextLayer(NeuralLayer layer) {
    nextLayer = null;
  }

  /**
   * setPreviousLayer
   * This method links this layer to the previous one
   *
   * @param layer Previous Layer
   */
  @Override
  public void setPreviousLayer(NeuralLayer layer) {
    previousLayer = layer;
    if (layer.nextLayer != this) {
      layer.setNextLayer(this);
    }
  }

}
