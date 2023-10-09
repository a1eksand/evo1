package netbase.impl;

import netbase.Linear;

public class InputNeuron extends Neuron {

  /**
   * InputNeuron constructor
   *
   * @see InputNeuron
   */
  public InputNeuron() {
    super(1, new Linear(1));
    bias = 0.0;
  }

  /**
   * init
   * Method for initialization of the input neuron, it just adds the weights
   * with 1's values and a 0 at the bias
   *
   * @see InputNeuron
   */
  @Override
  public void init(DoubleIterator doubleIterator) {
    this.weight[0] = 1.0;
    this.weight[1] = 0.0;
  }

}
