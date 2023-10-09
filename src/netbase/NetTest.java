package netbase;

import java.util.Objects;
import java.util.Random;
import java.util.random.RandomGenerator;
import static utils.Utils.toStringView;

public class NetTest {

  public static void main(String[] args) {
    var rnd = new Random(371L);
    double[] i1 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    double[] i2 = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    double[] i3 = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
    test(testNet(rnd), i1, "       6: 0.29062037 0.18502051 0.24990823 0.16837219 0.26161461 0.20552427");
    test(testNet(rnd), i2, "       6: 4.20659860 3.49913204 2.64289194 3.40767110 3.34819488 3.91674263");
    test(testNet(rnd), i3, "       6: 3.98680197 4.35537794 3.84427021 3.89166371 2.89748624 3.44371637");
    test(testNet(rnd), i1, "       6: 3.30223947 2.95229671 2.75028636 2.30203883 2.83564933 2.79168064");
    test(testNet(rnd), i2, "       6: 1.96271727 1.71328149 2.34190053 1.85994761 1.85743337 2.24676519");
    test(testNet(rnd), i3, "       6: 0.43596428 0.33033815 0.43779489 0.39713981 0.51536501 0.44621964");
  }

  static void test(Net net, double[] input, String exp) {
    double[] output = net.calc(input);
    var act = toStringView(output);
    if (!Objects.equals(act, exp)) {
      throw new RuntimeException("act: '%s' / exp: '%s'".formatted(act, exp));
    }
  }

  static Net testNet(RandomGenerator rmd) {
    return new Net(10, 10, 6, () -> rmd, Sigmoid::new);
  }
}

