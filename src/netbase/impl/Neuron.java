package netbase.impl;

import java.io.Serializable;
import netbase.ActivationFunction;


public class Neuron implements Serializable {

  /**
   * Weights associated with this Neuron
   */
  protected double[] weight;
  /**
   * Bias of the neuron. It should be always 1.0, except for the first layer.
   */
  protected double bias = 1.0;
  /**
   * Number of Inputs. If is 0, it means the neuron wasn't initialized yet.
   */
  private int numberOfInputs;
  /**
   * Activation function of this neuron
   */
  private ActivationFunction activationFunction;

  /**
   * Neuron constructor
   *
   * @param numberofinputs Number of inputs
   * @param iaf Activation function
   */
  public Neuron(int numberofinputs, ActivationFunction iaf) {
    numberOfInputs = numberofinputs;
    weight = new double[numberofinputs + 1];
    activationFunction = iaf;
  }

  /**
   * init
   * This method initializes the neuron by setting randomly its weights
   */
  public void init(DoubleIterator doubleIterator) {
    if (numberOfInputs > 0) {
      for (int i = 0; i <= numberOfInputs; i++) {
        this.weight[i] = doubleIterator.nextDouble();
      }
    }
  }

  /**
   * calc
   * Calculates the neuron's output
   */
  public double calc(double[] inputs) {
    double outputBeforeActivation = 0.0;
    for (int i = 0; i <= numberOfInputs; i++) {
      outputBeforeActivation += (i == numberOfInputs ? bias : inputs[i]) * weight[i];
    }
    return activationFunction.calc(outputBeforeActivation);
  }
}
