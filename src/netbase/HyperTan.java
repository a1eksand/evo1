package netbase;

public class HyperTan implements ActivationFunction {

  /**
   * Hyperbolic tangent coefficient
   */
  private double a = 1.0;

  /**
   * HyperTan dummy constructor
   */
  public HyperTan() {

  }

  /**
   * HyperTan consctrutor
   *
   * @param value new coefficient
   */
  public HyperTan(double value) {
    this.setA(value);
  }

  /**
   * setA
   * Sets a new coefficient for this function
   *
   * @param value new coefficient
   */
  public void setA(double value) {
    this.a = value;
  }

  /**
   * calc
   * Peforms the calculation of hyperbolic tangent of x
   *
   * @param x
   * @return Returns the hyperbolic tangent of x
   */
  @Override
  public double calc(double x) {
    return (1.0 - Math.exp(-a * x)) / (1.0 + Math.exp(-a * x));
  }

}
