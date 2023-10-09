package netbase;

public class Step implements ActivationFunction {

  /**
   * calc
   * Method that returns the result of the hardlimiting threshold function
   *
   * @param x
   * @return
   */
  @Override
  public double calc(double x) {
    if (x < 0) {
      return 0.0;
    } else {
      return 1.0;
    }
  }

}
