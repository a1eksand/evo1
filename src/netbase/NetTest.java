package netbase;

import java.util.Objects;
import java.util.Random;
import static utils.Utils.toStringView;

public class NetTest {

  public static void main(String[] args) {
    var rnd = new Random(371L);
    double[] i1 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
    double[] i2 = {1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
    double[] i3 = {0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5, 0.5};
    test(testNet(rnd), i1, "       6: 0.18093069 0.22778588 0.30694746 0.26031021 0.21709174 0.27333752");
    test(testNet(rnd), i2, "       6: 2.99582854 3.31284924 2.21315008 3.08672285 2.12240166 3.14083818");
    test(testNet(rnd), i3, "       6: 4.24108694 3.30154806 2.72934280 3.94362666 4.05871243 3.38576787");
    test(testNet(rnd), i1, "       6: 4.49872676 2.96459464 2.84338654 2.96477305 3.90389668 3.33007973");
    test(testNet(rnd), i2, "       6: 6.24395724 4.72506898 5.01337974 5.00008230 6.34257930 6.24274952");
    test(testNet(rnd), i3, "       6: 1.74583041 1.63809018 1.45797259 1.78643269 1.86220788 1.61239525");
  }

  static void test(Net net, double[] input, String exp) {
    double[] output = net.calc(input);
    var act = toStringView(output);
    if (!Objects.equals(act, exp)) {
      throw new RuntimeException("act: '%s' / exp: '%s'".formatted(act, exp));
    }
  }

  static Net testNet(Random random) {
    return new Net(10, 10, 6, random, Sigmoid::new);
  }
}

