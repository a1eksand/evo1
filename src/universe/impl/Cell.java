package universe.impl;

import java.awt.Color;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import netbase.Net;
import universe.Field;
import static universe.Field.ACTION_ATTACK_2_PRISE;
import static universe.Field.ACTION_COST_ATTACK_1;
import static universe.Field.ACTION_COST_ATTACK_2;
import static universe.Field.ACTION_COST_MOVE;
import static universe.Field.ACTION_COST_REGENERATE;
import static universe.Field.ACTION_COST_ROTATE_1;
import static universe.Field.ACTION_COST_ROTATE_2;
import static universe.Field.ACTION_SLEEP_PRISE;
import utils.Logger;
import utils.Utils;

public class Cell implements Supplier<Color>, Serializable {

  @Serial
  private static final long serialVersionUID = -2893434248830474937L;

  static final Logger LOGGER = Logger.Factory.getLogger(Cell.class);
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

  private final Random rnd;
  private final Net net;
  private final Color color;

  private final double[] input = new double[INPUT_SIZE];
  private final AtomicLong stepHolder = new AtomicLong(0L);
  private volatile D1 direction;
  private volatile int energy;
  private volatile int iteration;

  public Cell(Random rnd) {
    this(rnd, new Net(NET_SIZE, INPUT_SIZE, OUTPUT_SIZE, rnd), Field.INITIAL_ENERGY);
  }

  public Cell(Random rnd, double[] signature) {
    this(rnd, new Net(NET_SIZE, INPUT_SIZE, OUTPUT_SIZE, signature), Field.INITIAL_ENERGY);
  }

  Cell(Random rnd, Net net, int energy) {
    this.rnd = rnd;
    this.net = net;
    color = new Color(Utils.hash(net.getSignature()));
    direction = DIRECTIONS1[rnd.nextInt(DIRECTIONS1.length)];
    this.energy = energy;
    iteration = 0;
  }

  void update(World world, int x, int y, long step) {
    long lastStep = stepHolder.getAndSet(step);
    if (lastStep == step) {
      return;
    }

    if (iteration++ > Field.MAX_ITERATIONS || energy <= 0) {
      world.clear(x, y);
    }

    Cell front = null;
    Cell outside1;
    Cell outside2;

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
          case OUTPUT_MOVE -> move(world, x, y, front);
          case OUTPUT_ROTATE -> rotate1();
          case OUTPUT_SLEEP -> sleep();
          case OUTPUT_ATTACK1 -> attack1(world, x, y, front);
          case OUTPUT_ATTACK2 -> attack2(world, x, y, front);
          case OUTPUT_DIVIDE -> divide(world, x, y, front);
          case OUTPUT_REGENERATE -> regenerate(front);
        }
      }
    }
  }

  private void move(World world, int x, int y, Cell that) {
    LOGGER.trace("0x%016X: %s", getId(), "try move");
    int x2 = x + direction.x;
    int y2 = y + direction.y;
    if (Objects.isNull(that)) {
      if (world.move(x, y, x2, y2)) {
        LOGGER.trace("0x%016X: %s", getId(), "move");
        energy -= ACTION_COST_MOVE;
      }
    }
  }

  private void rotate1() {
    LOGGER.trace("0x%016X: %s", getId(), "rotate1");
    while (true) {
      var d = DIRECTIONS1[rnd.nextInt(DIRECTIONS1.length)];
      if (direction != d) {
        direction = d;
        energy -= ACTION_COST_ROTATE_1;
        break;
      }
    }
  }

  private void rotate2() {
    LOGGER.trace("0x%016X: %s", getId(), "rotate2");
    while (true) {
      var d = DIRECTIONS1[rnd.nextInt(DIRECTIONS1.length)];
      if (direction != d) {
        direction = d;
        energy -= ACTION_COST_ROTATE_2;
        break;
      }
    }
  }

  private void sleep() {
    LOGGER.trace("0x%016X: %s", getId(), "sleep");
    energy += ACTION_SLEEP_PRISE;
  }

  private void attack1(World world, int x, int y, Cell that) {
    LOGGER.trace("0x%016X: %s", getId(), "try attack1");
    int x2 = x + direction.x;
    int y2 = y + direction.y;
    if (Objects.nonNull(that)) {
      LOGGER.trace("0x%016X: %s > 0x%016X", getId(), "attack1", that.getId());
      if (that.energy < energy) {
        energy += that.energy - ACTION_COST_ATTACK_1;
        that.energy = 0;
        world.clear(x2, y2, that);

      } else {
        that.energy -= energy;
        energy *= 2;
      }
    }
  }

  private void attack2(World world, int x, int y, Cell that) {
    LOGGER.trace("0x%016X: %s", getId(), "try attack2");
    int x2 = x + direction.x;
    int y2 = y + direction.y;
    if (Objects.nonNull(that)) {
      LOGGER.trace("0x%016X: %s > 0x%016X", getId(), "attack2", that.getId());
      if (that.energy < ACTION_ATTACK_2_PRISE) {
        energy += that.energy - ACTION_COST_ATTACK_2;
        that.energy = 0;
        world.clear(x2, y2, that);
      } else {
        that.energy -= ACTION_ATTACK_2_PRISE;
        energy += ACTION_ATTACK_2_PRISE - ACTION_COST_ATTACK_2;
      }
    }
  }

  private void divide(World world, int x, int y, Cell that) {
    LOGGER.trace("0x%016X: %s", getId(), "try divide");
    int x2 = x + direction.x;
    int y2 = y + direction.y;
    if (Objects.isNull(that)) {
      if (world.add(new Cell(rnd, new Net(NET_SIZE, INPUT_SIZE, OUTPUT_SIZE, mutate(net.getSignature())), energy / 2), x2, y2)) {
        LOGGER.trace("0x%016X: %s", getId(), "divide");
        energy /= 2;
      }
    } else {
      rotate2();
    }
  }

  private double[] mutate(double[] signature) {
    int i = rnd.nextInt(signature.length * 50);
    if (i < signature.length) {
      double[] signatur3 = new double[signature.length];
      System.arraycopy(signature, 0, signatur3, 0, signature.length);
      signatur3[i] += (iteration % 2 == 0 ? 1 : -1) * rnd.nextDouble() / 100;
      return signatur3;
    }
    return signature;
  }

  private void regenerate(Cell that) {
    LOGGER.trace("0x%016X: %s", getId(), "try regenerate");
    if (Objects.nonNull(that)) {
      LOGGER.trace("0x%016X: %s > 0x%016X", getId(), "regenerate", that.getId());
      that.energy += ACTION_COST_REGENERATE;
      energy -= ACTION_COST_REGENERATE;
    }
  }

  private long getId() {
    return (long)color.getRGB() << 32 | (long)super.hashCode();
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
