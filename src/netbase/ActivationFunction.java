package netbase;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

public interface ActivationFunction extends Serializable {

  List<Function<Double, ? extends ActivationFunction>> functionFactories = List.of(HyperTan::new, Linear::new, Sigmoid::new, Step::new);

  /**
   * calc
   * This is the core method for calculating the activation function's value
   *
   * @param x
   * @return returns the result of the activation function given x
   */
  double calc(double x);
}
