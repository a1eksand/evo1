package netbase;

public class Step implements ActivationFunction {

  private final double a;

  public Step(double value) {
    this.a = value;
  }

  @Override
  public double calc(double x) {
    if (x < a) {
      return 0.0;
    } else {
      return 1.0;
    }
  }
}
