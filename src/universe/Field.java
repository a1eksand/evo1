package universe;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;
import universe.impl.Cell;
import utils.Utils;
import universe.impl.World;

public interface Field {

  int CONCURRENT_LEVEL = 4;
  long SEED = 37;
  int SIZE = 1000;
  int INITIAL_ENERGY = 200;
  int MAX_ITERATIONS = 200;

  int ACTION_COST_MOVE = 2;
  int ACTION_COST_ROTATE_1 = 0;
  int ACTION_COST_ROTATE_2 = 0;
  int ACTION_SLEEP_PRISE = 5;
  int ACTION_COST_ATTACK_1 = 15;
  int ACTION_COST_ATTACK_2 = 5;
  int ACTION_ATTACK_2_PRISE = 10;
  int ACTION_COST_REGENERATE = 10;

  static Field rnd(long seed, int size, int count) {
    var rnd = new Random(seed);
    var w = new World(size, rnd);
    for (int i = 0; i < count; i++) {
      w.addCell(new Cell(rnd));
    }
    return w;
  }

  static Field load(InputStream in) {
    return Utils.deserialize(in);
  }

  static Field load(BufferedReader csv, long seed, int size) {
    var rnd = new Random(seed);
    var w = new World(size, rnd);
    csv.lines()
        .map(line -> line.split(","))
        .map(line -> Arrays.stream(line).mapToDouble(Double::valueOf).toArray())
        .forEach(doubles -> w.addCell(new Cell(rnd, Arrays.copyOfRange(doubles, 2, doubles.length)), (int) doubles[0], (int) doubles[1]));
    return w;
  }

  Supplier<Color>[][] getState();

  long getStep();

  void updaters(RecursiveTask<Integer>[] updaters);

  void serialize(OutputStream out);

  void snapshot(BufferedWriter out);
}
