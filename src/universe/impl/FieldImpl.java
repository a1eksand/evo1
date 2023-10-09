package universe.impl;

import java.awt.Color;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import netbase.Net;
import universe.Field;
import static universe.Field.Var.ACTION_ATTACK_2_PRISE;
import static universe.Field.Var.ACTION_COST_ATTACK_1;
import static universe.Field.Var.ACTION_COST_ATTACK_2;
import static universe.Field.Var.ACTION_COST_MOVE;
import static universe.Field.Var.ACTION_COST_REGENERATE;
import static universe.Field.Var.ACTION_COST_ROTATE_1;
import static universe.Field.Var.ACTION_COST_ROTATE_2;
import static universe.Field.Var.ACTION_SLEEP_PRISE;
import static universe.Field.Var.INITIAL_ENERGY;
import static universe.Field.Var.MAX_ITERATIONS;
import utils.TLQueue;
import utils.Utils;

public class FieldImpl implements Field, Serializable {

  @Serial
  private static final long serialVersionUID = -5537671945950711884L;

  protected volatile int[] vars = new int[Var.values().length];
  protected final CellImpl[][] field;
  private final AtomicLong iteration = new AtomicLong(0L);
  private final Supplier<Color>[][] state;
  private final Supplier<RandomGenerator> rnd;

  private final TLQueue<CellImpl> accumulator = new TLQueue<>();

  public FieldImpl(int fieldSize, Supplier<RandomGenerator> rnd) {
    this.field = new CellImpl[fieldSize][fieldSize];
    this.state = new CellImpl[fieldSize][fieldSize];
    this.rnd = rnd;
    vars[INITIAL_ENERGY.index] = INITIAL_ENERGY.defau1t;
    vars[MAX_ITERATIONS.index] = MAX_ITERATIONS.defau1t;
    vars[ACTION_COST_MOVE.index] = ACTION_COST_MOVE.defau1t;
    vars[ACTION_COST_ROTATE_1.index] = ACTION_COST_ROTATE_1.defau1t;
    vars[ACTION_COST_ROTATE_2.index] = ACTION_COST_ROTATE_2.defau1t;
    vars[ACTION_SLEEP_PRISE.index] = ACTION_SLEEP_PRISE.defau1t;
    vars[ACTION_COST_ATTACK_1.index] = ACTION_COST_ATTACK_1.defau1t;
    vars[ACTION_COST_ATTACK_2.index] = ACTION_COST_ATTACK_2.defau1t;
    vars[ACTION_ATTACK_2_PRISE.index] = ACTION_ATTACK_2_PRISE.defau1t;
    vars[ACTION_COST_REGENERATE.index] = ACTION_COST_REGENERATE.defau1t;
  }

  public synchronized void addCell(CellImpl cell) {
    while (true) {
      int x = rnd.get().nextInt(field.length);
      int y = rnd.get().nextInt(field.length);
      if (Objects.isNull(field[x][y])) {
        field[x][y] = cell;
        break;
      }
    }
  }

  public synchronized void addCell(CellImpl cell, int x, int y) {
    field[x][y] = cell;
  }

  @Override
  public Supplier<Color>[][] getState() {
    for (int x = 0; x < field.length; x++) {
      System.arraycopy(field[x], 0, state[x], 0, field.length);
    }
    return state;
  }

  @Override
  public synchronized void serialize(OutputStream out) {
    Utils.serialize(this, out);
  }

  @Override
  public synchronized void snapshot(Writer writer) {
    try (Closeable closeable = writer) {
      for (int x = 0; x < field.length; x++) {
        for (int y = 0; y < field[x].length; y++) {
          if (Objects.nonNull(field[x][y])) {
            var sig = field[x][y].getSignature();
            writer.write(String.valueOf(x));
            writer.write(",");
            writer.write(String.valueOf(y));
            writer.write(",");
            for (int i = 0; i < sig.length; i++) {
              writer.write(String.valueOf(sig[i]));
              if (i != sig.length - 1) {
                writer.write(",");
              }
            }
            writer.write("\n");
          }
        }
      }
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int[] getVars() {
    return vars;
  }

  @Override
  public void setVar(Var var, int value) {
    int[] vars = this.vars.clone();
    vars[var.index] = value;
    this.vars = vars;
  }

  @Override
  public long getStep() {
    return iteration.get();
  }

  @Override
  public void nextStep() {
    iteration.incrementAndGet();
  }

  @Override
  public long update(int x1, int x2, int y1, int y2) {
    long count = 0;
    for (int x = x1; x <= x2; x++) {
      for (int y = y1; y <= y2; y++) {
        if (Objects.nonNull(field[x][y])) {
          if (field[x][y].update(this, x, y, iteration.get(), vars)) {
            count++;
          }
        }
      }
    }
    return count;
  }

  synchronized CellImpl get(int x2, int y2) {
    x2 = x(x2);
    y2 = y(y2);
    return field[x2][y2];
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

  public CellImpl add(Supplier<RandomGenerator> rnd, Net net, int energy, int x2, int y2) {
    var cell = accumulator.get().poll();
    if (Objects.nonNull(cell)) {
      cell.reactivate(net, energy);
    } else {
      cell = new CellImpl(rnd, net, energy);
    }
    if (add(cell, x2, y2)) {
      return cell;
    } else {
      cell.deactivate();
      accumulator.get().add(cell);
      return null;
    }
  }

  private synchronized boolean add(CellImpl cell, int x2, int y2) {
    x2 = x(x2);
    y2 = y(y2);
    if (Objects.nonNull(field[x2][y2])) {
      return false;
    }
    field[x2][y2] = cell;
    return true;
  }

  void clear(int x2, int y2, CellImpl cell) {
    clear(cell, x2, y2);
    cell.deactivate();
    accumulator.get().add(cell);
  }

  private synchronized void clear(CellImpl cell, int x2, int y2) {
    x2 = x(x2);
    y2 = y(y2);
    if (field[x2][y2] == cell) {
      field[x2][y2] = null;
    } else {
      throw new RuntimeException("Cell %s not found".formatted(cell));
    }
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
}
