package universe.impl;

import java.awt.Color;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import universe.Field;
import utils.Utils;

public class World implements Field, Serializable {

  @Serial
  private static final long serialVersionUID = -5537671945950711884L;

  private final Cell[][] field;
  private final Supplier<Color>[][] state;
  private final Random rnd;

  private final AtomicLong iteration = new AtomicLong(0L);

  public World(int x, Random rnd) {
    this.field = new Cell[x][x];
    this.state = new Cell[x][x];
    this.rnd = rnd;
  }

  public void addCell(Cell cell) {
    while (true) {
      int x = rnd.nextInt(field.length);
      int y = rnd.nextInt(field.length);
      if (Objects.isNull(field[x][y])) {
        field[x][y] = cell;
        break;
      }
    }
  }

  @Override
  synchronized public Supplier<Color>[][] getState() {
    for (int x = 0; x < field.length; x++) {
      System.arraycopy(field[x], 0, state[x], 0, field.length);
    }
    return state;
  }

  @Override
  public long getStep() {
    return iteration.get();
  }

  @Override
  public void updaters(RecursiveTask<Integer>[] updaters) {
    long set = iteration.incrementAndGet();
    for (int i = 0; i < updaters.length; i++) {
      updaters[i] = new Updater(i, set);
    }
  }

  @Override
  public synchronized void save(OutputStream out) {
    Utils.serialize(this, out);
  }

  private int update(int x1, int x2, int y1, int y2, long s) {
    int count = 0;
    for (int x = x1; x <= x2; x++) {
      for (int y = y1; y <= y2; y++) {
        if (Objects.nonNull(field[x][y])) {
          count++;
          field[x][y].update(this, x, y, s);
        }
      }
    }
    return count;
  }

  synchronized Cell get(int x, int y) {
    return field[x(x)][y(y)];
  }

  synchronized boolean move(int x1, int y1, int x2, int y2) {
    x2 = x(x2);
    y2 = y(y2);
    if (Objects.nonNull(field[x2][y2])) {
      return false;
    }
    field[x2][y2] = field[x1][y1];
    field[x1][y1] = null;
    return true;
  }

  synchronized boolean add(Cell cell, int x2, int y2) {
    x2 = x(x2);
    y2 = y(y2);
    if (Objects.nonNull(field[x2][y2])) {
      return false;
    }
    field[x2][y2] = cell;
    return true;
  }

  synchronized void clear(int x, int y) {
    field[x(x)][y(y)] = null;
  }

  private int x(int x) {
    if (x == -1) {
      return field.length - 1;
    } else if (x == field.length) {
      return 0;
    }
    return x;
  }

  private int y(int y) {
    if (y == -1) {
      return field[0].length - 1;
    } else if (y == field[0].length) {
      return 0;
    }
    return y;
  }

  private class Updater extends RecursiveTask<Integer> {

    private final int i;
    private final long s;

    private Updater(int i, long s) {
      this.i = i;
      this.s = s;
    }

    @Override
    protected Integer compute() {
      return switch (i) {
        case 0  -> update(0, field.length / 2, 0, field[0].length / 2, s);
        case 1  -> update(field.length / 2 + 1, field.length - 1, 0, field[0].length / 2, s);
        case 2  -> update(0, field.length / 2, field[0].length / 2 + 1, field[0].length - 1, s);
        case 3  -> update(field.length / 2 + 1, field.length - 1, field[0].length / 2 + 1, field[0].length - 1, s);
        default -> throw new IllegalStateException("Unexpected value: " + i);
      };
    }
  }
}
