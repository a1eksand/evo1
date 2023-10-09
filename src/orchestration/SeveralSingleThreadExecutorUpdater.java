package orchestration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import orchestration.Orchestrator.Updater;
import universe.Field;

public class SeveralSingleThreadExecutorUpdater implements Updater {

  private final int fieldSize;
  private final int updatableSubSize;
  private final ExecutorService[] executors;
  private final Future<Long>[] futures;

  public SeveralSingleThreadExecutorUpdater(int fieldSize, int concurrencyLevel) {
    if (fieldSize % (concurrencyLevel * 2) > 0) {
      throw new RuntimeException("World has not been initialized!");
    }
    this.fieldSize = fieldSize;
    this.updatableSubSize = fieldSize / (concurrencyLevel * 2);
    this.executors = new ExecutorService[concurrencyLevel];
    this.futures = new Future[concurrencyLevel];
    for (int i = 0; i < concurrencyLevel; i++) {
      this.executors[i] = Executors.newSingleThreadExecutor();
    }
  }

  @Override
  public Updater init(Supplier<RandomGenerator> rnd) {
    for (int i = 0; i < executors.length; i++) {
      try {
        this.executors[i].submit(() -> rnd.get().nextLong()).get();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        Thread.currentThread().interrupt();
      }
    }
    return this;
  }

  @Override
  public long update(Field field) {
    try {
      field.nextStep();
      return concurrentUpdate(field);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return 0;
    }
  }

  @Override
  public void stop() {
    for (int i = 0; i < executors.length; i++) {
      executors[i].shutdown();
    }
  }

  private long concurrentUpdate(Field field) throws ExecutionException, InterruptedException {
    long cellCount = 0;

    for (int i = 0; i < executors.length; i++) {
      int index = i * 2;
      futures[i] = executors[i].submit(() -> updateSubField(index, field));
    }
    for (int i = 0; i < futures.length; i++) {
      cellCount += futures[i].get();
    }

    for (int i = 0; i < executors.length; i++) {
      int index = i * 2 + 1;
      futures[i] = executors[i].submit(() -> updateSubField(index, field));
    }
    for (int i = 0; i < futures.length; i++) {
      cellCount += futures[i].get();
    }

    return cellCount;
  }

  private long updateSubField(int index, Field field) {
    return field.update(index * updatableSubSize, ((index + 1) * updatableSubSize) - 1, 0, fieldSize - 1);
  }
}
