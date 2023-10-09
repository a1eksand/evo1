package netbase;

public class Sigmoid implements ActivationFunction {

  private final double a;

  public Sigmoid(double value) {
    this.a = value;
  }

  @Override
  public double calc(double x) {
    return 1.0 / (1.0 + Math.exp(-a * x));
  }
}
