package netbase;

public class Linear implements ActivationFunction {

  private final double a;

  public Linear(double value) {
    this.a = value;
  }

  @Override
  public double calc(double x) {
    return a * x;
  }
}
