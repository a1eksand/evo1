package universe.impl;

import java.awt.Color;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import netbase.Net;
import static universe.Field.Var.ACTION_ATTACK_2_PRISE;
import static universe.Field.Var.ACTION_COST_ATTACK_1;
import static universe.Field.Var.ACTION_COST_ATTACK_2;
import static universe.Field.Var.ACTION_COST_MOVE;
import static universe.Field.Var.ACTION_COST_REGENERATE;
import static universe.Field.Var.ACTION_COST_ROTATE_1;
import static universe.Field.Var.ACTION_COST_ROTATE_2;
import static universe.Field.Var.ACTION_SLEEP_PRISE;
import static universe.Field.Var.MAX_ITERATIONS;
import utils.Logger;
import utils.Utils;

public class CellImpl implements Supplier<Color>, Serializable {

  @Serial
  private static final long serialVersionUID = -2893434248830474937L;

  static final Logger LOGGER = Logger.Factory.getLogger(CellImpl.class);
  static final int NET_SIZE = 7;
  static final int INPUT_SIZE = 7;
  static final int OUTPUT_SIZE = 7;

  final static int INPUT_SELF_ENERGY = 0;
  final static int INPUT_ENERGY_FRONT = 1;
  final static int INPUT_ENERGY_SUBTOTAL = 2;
  final static int INPUT_ENERGY_TOTAL = 3;
  final static int INPUT_NET_DIFF_FRONT = 4;
  final static int INPUT_NET_DIFF_SOMEWHERE = 5;
  final static int INPUT_FRONT_IS_FREE = 6;


  final static int OUTPUT_MOVE = 0;
  final static int OUTPUT_ROTATE = 1;
  final static int OUTPUT_SLEEP = 2;
  final static int OUTPUT_ATTACK1 = 3;
  final static int OUTPUT_ATTACK2 = 4;
  final static int OUTPUT_DIVIDE = 5;
  final static int OUTPUT_REGENERATE = 6;

  private final static D1[] DIRECTIONS1 = D1.values();
  private final static D2[] DIRECTIONS2 = D2.values();

  private final AtomicLong stepHolder = new AtomicLong(0L);
  private final double[] input = new double[INPUT_SIZE];
  private final Supplier<RandomGenerator> rnd;
  private volatile Color color;
  private volatile D1 direction;
  private volatile Net net;
  private volatile int energy;
  private volatile int iteration;

  public CellImpl(Supplier<RandomGenerator> rnd, int initialEnergy) {
    this(rnd, new Net(NET_SIZE, INPUT_SIZE, OUTPUT_SIZE, rnd), initialEnergy);
  }

  public CellImpl(Supplier<RandomGenerator> rnd, double[] signature, int initialEnergy) {
    this(rnd, new Net(NET_SIZE, INPUT_SIZE, OUTPUT_SIZE, signature), initialEnergy);
  }

  CellImpl(Supplier<RandomGenerator> rnd, Net net, int energy) {
    this.rnd = rnd;

    this.color = new Color(Utils.hash(net.getSignature()));
    this.direction = DIRECTIONS1[rnd.get().nextInt(DIRECTIONS1.length)];
    this.energy = energy;
    this.iteration = 0;
    this.net = net;
  }

  void reactivate(Net net, int energy) {
    this.color = new Color(Utils.hash(net.getSignature()));
    this.direction = DIRECTIONS1[rnd.get().nextInt(DIRECTIONS1.length)];
    this.energy = energy;
    this.iteration = 0;
    this.net = net;
  }

  void deactivate() {
    stepHolder.set(0);

    this.color = null;
    this.direction = null;
    this.energy = 0;
    this.iteration = 0;
    this.net = null;
  }

  boolean update(FieldImpl world, int x, int y, long step, int[] vars) {
    long lastStep = stepHolder.getAndSet(step);
    if (lastStep == step) {
      return false;
    } else if (lastStep > 0 && lastStep != step - 1) {
      throw new RuntimeException("lastStep != step - 1");
    }

    if (iteration++ > vars[MAX_ITERATIONS.index] || energy <= 0) {
      LOGGER.trace("%s[%d]: %s", this, energy, "die.");
      world.clear(x, y, this);
      return false;
    }

    CellImpl front = null;
    CellImpl outside1;
    CellImpl outside2;

    input[INPUT_SELF_ENERGY] = energy;

    input[INPUT_ENERGY_SUBTOTAL] = 0;
    input[INPUT_ENERGY_TOTAL] = 0;
    input[INPUT_NET_DIFF_SOMEWHERE] = 0;

    for (int i = 0; i < DIRECTIONS1.length; i++) {
      outside1 = world.get(x + DIRECTIONS1[i].x, y + DIRECTIONS1[i].y);
      outside2 = world.get(x + DIRECTIONS2[i].x, y + DIRECTIONS2[i].y);

      if (direction == DIRECTIONS1[i]) {
        front = outside1;
        input[INPUT_ENERGY_FRONT] = Objects.nonNull(outside1) ? outside1.energy : 0;
        input[INPUT_NET_DIFF_FRONT] = Objects.nonNull(outside1) ? Utils.diff(net.getSignature(), outside1.net.getSignature()) : 0;
        input[INPUT_FRONT_IS_FREE] = Objects.nonNull(outside1) ? 1 : 0;
      }

      input[INPUT_ENERGY_SUBTOTAL] += Objects.nonNull(outside1) ? outside1.energy : 0;
      input[INPUT_ENERGY_TOTAL] += Objects.nonNull(outside1) ? outside1.energy : 0;
      input[INPUT_ENERGY_TOTAL] += Objects.nonNull(outside2) ? outside2.energy : 0;

      input[INPUT_NET_DIFF_SOMEWHERE] += Objects.nonNull(outside1) ? Utils.diff(net.getSignature(), outside1.net.getSignature()) : 0;
    }

    double[] outputs = net.calc(input);

    double max = 0;
    for (int i = 0; i < OUTPUT_SIZE; i++) {
      max = Math.max(max, outputs[i]);
    }
    for (int i = 0; i < OUTPUT_SIZE; i++) {
      if (max == outputs[i]) {
        switch (i) {
          case OUTPUT_MOVE -> move(world, x, y, front, vars);
          case OUTPUT_ROTATE -> rotate1(vars);
          case OUTPUT_SLEEP -> sleep(vars);
          case OUTPUT_ATTACK1 -> attack1(world, x, y, front, vars);
          case OUTPUT_ATTACK2 -> attack2(world, x, y, front, vars);
          case OUTPUT_DIVIDE -> divide(world, x, y, front, vars);
          case OUTPUT_REGENERATE -> regenerate(front, vars);
        }
      }
    }
    return true;
  }

  private void move(FieldImpl world, int x, int y, CellImpl that, int[] vars) {
    LOGGER.trace("%s[%d]: %s", this, energy, "try move");
    int x2 = x + direction.x;
    int y2 = y + direction.y;
    if (Objects.isNull(that)) {
      if (world.move(x, y, x2, y2)) {
        energy -= vars[ACTION_COST_MOVE.index];
        LOGGER.trace("%s[%d]: %s", this, energy, "moved");
      }
    }
  }

  private void rotate1(int[] vars) {
    LOGGER.trace("%s[%d]: %s", this, energy, "try rotate1");
    while (true) {
      var d = DIRECTIONS1[rnd.get().nextInt(DIRECTIONS1.length)];
      if (direction != d) {
        direction = d;
        energy -= vars[ACTION_COST_ROTATE_1.index];
        LOGGER.trace("%s[%d]: %s", this, energy, "rotated1");
        break;
      }
    }
  }

  private void rotate2(int[] vars) {
    LOGGER.trace("%s[%d]: %s", this, energy, "try rotate2");
    while (true) {
      var d = DIRECTIONS1[rnd.get().nextInt(DIRECTIONS1.length)];
      if (direction != d) {
        direction = d;
        energy -= vars[ACTION_COST_ROTATE_2.index];
        LOGGER.trace("%s[%d]: %s", this, energy, "rotated2");
        break;
      }
    }
  }

  private void sleep(int[] vars) {
    energy += vars[ACTION_SLEEP_PRISE.index];
    LOGGER.trace("%s[%d]: %s", this, energy, "slept");
  }

  private void attack1(FieldImpl world, int x, int y, CellImpl that, int[] vars) {
    LOGGER.trace("%s[%d]: %s", this, energy, "try attack1");
    int x2 = x + direction.x;
    int y2 = y + direction.y;
    if (Objects.nonNull(that)) {
      LOGGER.trace("%s[%d]: %s > %s[%d]", this, energy, "attack1", that, that.energy);
      if (that.energy < energy) {
        energy += that.energy - vars[ACTION_COST_ATTACK_1.index];
        that.energy = 0;
        LOGGER.trace("%s[%d]: %s > %s[%d]", this, energy, "attacked1", that, that.energy);
        LOGGER.trace("%s[%d]: %s", that, that.energy, "killed1.");
        world.clear(x2, y2, that);
      } else {
        that.energy -= energy;
        energy *= 2;
        LOGGER.trace("%s[%d]: %s > %s[%d]", this, energy, "attacked1", that, that.energy);
      }
    }
  }

  private void attack2(FieldImpl world, int x, int y, CellImpl that, int[] vars) {
    LOGGER.trace("%s[%d]: %s", this, energy, "try attack2");
    int x2 = x + direction.x;
    int y2 = y + direction.y;
    if (Objects.nonNull(that)) {
      LOGGER.trace("%s[%d]: %s > %s[%d]", this, energy, "attack2", that, that.energy);
      if (that.energy < vars[ACTION_ATTACK_2_PRISE.index]) {
        energy += that.energy - vars[ACTION_COST_ATTACK_2.index];
        that.energy = 0;
        LOGGER.trace("%s[%d]: %s > %s[%d]", this, energy, "attacked2", that, that.energy);
        LOGGER.trace("%s[%d]: %s", that, that.energy, "killed2.");
        world.clear(x2, y2, that);
      } else {
        that.energy -= vars[ACTION_ATTACK_2_PRISE.index];
        energy += vars[ACTION_ATTACK_2_PRISE.index] - vars[ACTION_COST_ATTACK_2.index];
        LOGGER.trace("%s[%d]: %s > %s[%d]", this, energy, "attacked2", that, that.energy);
      }
    }
  }

  private void divide(FieldImpl world, int x, int y, CellImpl that, int[] vars) {
    LOGGER.trace("%s[%d]: %s", this, energy, "try divide");
    int x2 = x + direction.x;
    int y2 = y + direction.y;
    if (Objects.isNull(that)) {
      that = world.add(rnd, mutate(net), energy / 2, x2, y2);
      if (Objects.nonNull(that)) {
        energy /= 2;
        LOGGER.trace("%s[%d]: %s > %s[%d]", this, energy, "divided", that, that.energy);
      }
    } else {
      rotate2(vars);
    }
  }

  private Net mutate(Net net) {
    double[] signature = net.getSignature();
    int i = rnd.get().nextInt(signature.length * 50);
    if (i < signature.length) {
      double[] mutatedSignature = new double[signature.length];
      System.arraycopy(signature, 0, mutatedSignature, 0, signature.length);
      mutatedSignature[i] += (iteration % 2 == 0 ? 1 : -1) * rnd.get().nextDouble() / 100;
      return new Net(NET_SIZE, INPUT_SIZE, OUTPUT_SIZE, mutatedSignature);
    }
    return net;
  }

  private void regenerate(CellImpl that, int[] vars) {
    LOGGER.trace("%s[%d]: %s", this, energy, "try regenerate");
    if (Objects.nonNull(that)) {
      that.energy += vars[ACTION_COST_REGENERATE.index];
      energy -= vars[ACTION_COST_REGENERATE.index];
      LOGGER.trace("%s[%d]: %s > %s[%d]", this, energy, "regenerated", that, that.energy);
    }
  }

  private long id() {
    return (((long) hashCode()) << 32) | (net.hashCode() & 0xFFFFFFFFL);
  }

  @Override
  public String toString() {
    return "0x%016X".formatted(id());
  }

  @Override
  public Color get() {
    return color;
  }

  double[] getSignature() {
    return net.getSignature();
  }

  private enum D1 {
    E(1, 0),
    N(0, 1),
    W(-1, 0),
    S(0, -1),
    ;
    final int x;
    final int y;

    D1(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  private enum D2 {
    NE(1, 1),
    NW(-1, 1),
    SW(-1, -1),
    SE(1, -1),
    ;
    final int x;
    final int y;

    D2(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }
}
