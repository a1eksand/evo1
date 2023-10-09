package universe;

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;
import universe.impl.Cell;
import universe.impl.World;

public interface Field {

  int CONCURRENT_LEVEL = 4;
  long SEED = 37;
  int INITIAL_ENERGY = 200;
  int MAX_ITERATIONS = 200;

  int ACTION_COST_MOVE = 2;
  int ACTION_COST_ROTATE = 0;
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

  Supplier<Color>[][] getState();

  long getStep();

  void updaters(RecursiveTask<Integer>[] updaters);
}
