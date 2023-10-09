package netbase;

public class HyperTan implements ActivationFunction {

  private final double a;

  public HyperTan(double value) {
    this.a = value;
  }

  @Override
  public double calc(double x) {
    return (1.0 - Math.exp(-a * x)) / (1.0 + Math.exp(-a * x));
  }
}
