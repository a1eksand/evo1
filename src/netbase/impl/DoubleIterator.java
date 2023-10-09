package netbase.impl;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public abstract class DoubleIterator {

  public static DoubleIterator of(Supplier<RandomGenerator> rnd) {
    return new IteratorRandomImpl(rnd);
  }

  public static DoubleIterator of(double[] values) {
    return new IteratorValuesImpl(values);
  }

  public abstract double nextDouble();

  public abstract double[] getArray();

  private static class IteratorRandomImpl extends DoubleIterator {

    private final Supplier<RandomGenerator> rnd;
    private final ArrayList<Double> values = new ArrayList<>();

    private IteratorRandomImpl(Supplier<RandomGenerator> rnd) {
      this.rnd = rnd;
    }

    public double nextDouble() {
      double next = rnd.get().nextDouble();
      values.add(next);
      return next;
    }

    public double[] getArray() {
      return values.stream().mapToDouble(d -> d).toArray();
    }
  }

  private static class IteratorValuesImpl extends DoubleIterator {

    private final double[] values;
    private int pointer = 0;

    private IteratorValuesImpl(double[] values) {
      this.values = values;
    }

    public double nextDouble() {
      return values[pointer++];
    }

    public double[] getArray() {
      return values;
    }
  }
}
