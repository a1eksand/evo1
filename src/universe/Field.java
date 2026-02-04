package universe;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import universe.impl.FieldImpl;
import universe.impl.CellImpl;
import utils.Utils;

public interface Field {

  long SEED = 37;
  int SIZE = 1024;

  static Field create(Supplier<RandomGenerator> rnd, int fieldSize, int initialCellCount) {
    var world = new FieldImpl(fieldSize, rnd);
    for (int i = 0; i < initialCellCount; i++) {
      world.addCell(new CellImpl(rnd, Var.INITIAL_ENERGY.defau1t));
    }
    return world;
  }

  static Field create(InputStream in) {
    return Utils.deserialize(in);
  }

  static Field create(BufferedReader csv, Supplier<RandomGenerator> rnd, int fieldSize) {
    var world = new FieldImpl(fieldSize, rnd);
    csv.lines()
        .map(line -> line.split(","))
        .map(line -> Arrays.stream(line).mapToDouble(Double::valueOf).toArray())
        .forEach(doubles -> world.addCell(new CellImpl(rnd, Arrays.copyOfRange(doubles, 2, doubles.length), Var.INITIAL_ENERGY.defau1t), (int) doubles[0], (int) doubles[1]));
    return world;
  }

  Supplier<Color>[][] getState();

  long getStep();

  void nextStep();

  long update(int x1, int x2, int y1, int y2);

  void serialize(OutputStream out);

  void snapshot(Writer out);

  int[] getVars();

  void setVar(Var var, int value);

  enum Var {
    INITIAL_ENERGY(0, 20),
    MAX_ITERATIONS(1, 25),
    ACTION_COST_MOVE(2, 5),
    ACTION_COST_ROTATE_1(3, 1),
    ACTION_COST_ROTATE_2(4, 1),
    ACTION_SLEEP_PRISE(5, 5),
    ACTION_COST_ATTACK_1(6, 15),
    ACTION_COST_ATTACK_2(7, 5),
    ACTION_ATTACK_2_PRISE(8, 10),
    ACTION_COST_REGENERATE(9, 10),
    ;

    public final int index;
    public final int defau1t;

    Var(int index, int defau1t) {
      this.index = index;
      this.defau1t = defau1t;
    }
  }
}
