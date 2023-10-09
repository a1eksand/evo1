package netbase;

import java.io.Serializable;

public interface ActivationFunction extends Serializable {
  /**
   * calc
   * This is the core method for calculating the activation function's value
   *
   * @param x
   * @return returns the result of the activation function given x
   */
  double calc(double x);

  /**
   * ActivationFunctionENUM
   * This enumeration lists some of the common used activation functions. The
   * utility is to store this value as a neural network property
   */
  enum ActivationFunctionENUM {
    STEP, LINEAR, SIGMOID, HYPERTAN
  }

}
