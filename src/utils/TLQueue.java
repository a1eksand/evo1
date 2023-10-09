package utils;

import java.util.LinkedList;
import java.util.Queue;

public class TLQueue<E> extends ThreadLocal<Queue<E>> {

  @Override
  public Queue<E> initialValue() {
    return new LinkedList<E>();
  }
}
