package orchestration;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import universe.Field;

public class FixedThreadPoolUpdater implements Orchestrator.Updater {

  private final int fieldSize;
  private final int updatableSubSize;
  private final Future<Long>[] futures;
  private final ExecutorService executorService;

  public FixedThreadPoolUpdater(int fieldSize, int concurrencyLevel) {
    if (fieldSize % (concurrencyLevel * 2) > 0) {
      throw new RuntimeException("World has not been initialized!");
    }
    this.fieldSize = fieldSize;
    this.updatableSubSize = fieldSize / (concurrencyLevel * 2);
    this.futures = new Future[concurrencyLevel];
    this.executorService = Executors.newFixedThreadPool(concurrencyLevel);
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
    executorService.shutdown();
  }

  private long concurrentUpdate(Field field) throws ExecutionException, InterruptedException {
    long cellCount = 0;



    for (int i = 0; i < futures.length; i++) {
      int index = i * 2;
      futures[i] = executorService.submit(() -> updateSubField(index, field));
    }
    for (int i = 0; i < futures.length; i++) {
      cellCount += futures[i].get();
    }

    for (int i = 0; i < futures.length; i++) {
      int index = i * 2 + 1;
      futures[i] = executorService.submit(() -> updateSubField(index, field));
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
