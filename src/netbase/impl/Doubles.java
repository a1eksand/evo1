package netbase.impl;


import java.io.Serializable;
import java.util.Arrays;

public class Doubles implements Serializable {

  private final double[] array;

  public Doubles(int size) {
    array = new double[size];
  }

  public Doubles(double[] array) {
    this.array = array;
  }

  public double get(int i) {
    return array[i];
  }

  public void set(int i, double value) {
    array[i] = value;
  }

  public void set(double[] values) {
    System.arraycopy(values, 0, array, 0, Math.min(values.length, array.length));
  }

  public void add(double value) {
    throw new RuntimeException("add not supported");
  }

  public int size() {
    return array.length;
  }

  public double[] getArray() {
    return array;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Doubles that = (Doubles) o;
    return Arrays.equals(array, that.array);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(array);
  }
}
