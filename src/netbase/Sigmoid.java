package netbase;

public class Sigmoid implements ActivationFunction {
  /**
   * Coefficient in the sigmoid function
   */
  private double a = 1.0;

  /**
   * Sigmoid dummy constructor
   */
  public Sigmoid() {

  }

  /**
   * Sigmoid constructor
   *
   * @param value new coefficient for the sigmoid function
   */
  public Sigmoid(double value) {
    this.setA(value);
  }

  /**
   * setA
   * Sets a new coefficient for the sigmoid constructor
   *
   * @param value
   */
  public void setA(double value) {
    this.a = value;
  }

  /**
   * calc
   * Performs the calculation of this function
   *
   * @param x
   * @return Returns the result of this function
   */
  @Override
  public double calc(double x) {
    return 1.0 / (1.0 + Math.exp(-a * x));
  }
}
