package utils;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class TLRandom extends ThreadLocal<RandomGenerator> implements Supplier<RandomGenerator> {

  private final AtomicLong seed;

  public TLRandom(long seed) {
    this.seed = new AtomicLong(seed);
  }

  @Override
  public RandomGenerator initialValue() {
    return new Random(seed.getAndIncrement());
  }
}
