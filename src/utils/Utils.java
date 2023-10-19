package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class Utils {

  public static byte[] serialize(Serializable object) {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(object);
      return bos.toByteArray();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T deserialize(byte[] data) {
    try (ByteArrayInputStream bis = new ByteArrayInputStream(data); ObjectInputStream ois = new ObjectInputStream(bis)) {
      return  (T) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static void serialize(Serializable object, OutputStream out) {
    try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
      oos.writeObject(object);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T deserialize(InputStream in) {
    try (ObjectInputStream ois = new ObjectInputStream(in)) {
      return  (T) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static String toStringView(byte[] data) {
    var view = new StringBuilder();
    view.append("%8d:".formatted(data.length));
    for (int i = 0; i < data.length; i++) {
      view.append(" %02X".formatted(data[i]));
    }
    return view.toString();
  }

  public static String toStringView(double[] data) {
    var view = new StringBuilder();
    view.append("%8d:".formatted(data.length));
    for (int i = 0; i < data.length; i++) {
      view.append(" %02.8f".formatted(data[i]));
    }
    return view.toString();
  }

  public static int hash(double[] data) {
    int sum = 0;
    for (int i = 0; i < data.length; i++) {
      sum ^= (int)(data[i] * 1000000);
    }
    return sum;
  }

  public static double diff(double[] data1, double[] data2) {
    double diff = 0.0;
    int length = Math.max(data1.length, data2.length);
    for (int i = 0; i < length; i++) {
      diff += Math.abs((i < data1.length ? data1[i] : 0) - (i < data2.length ? data2[i] : 0));
    }
    return diff / length;
  }
}
