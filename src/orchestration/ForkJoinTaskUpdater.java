package orchestration;

import java.util.concurrent.RecursiveTask;
import universe.Field;

public class ForkJoinTaskUpdater implements Orchestrator.Updater {

  private final int fieldSize;
  private final int updatableSubSize;
  private final RecursiveTask<Long>[] updaters;

  public ForkJoinTaskUpdater(int fieldSize, int concurrencyLevel) {
    if (fieldSize % (concurrencyLevel * 2) > 0) {
      throw new RuntimeException("World has not been initialized!");
    }
    this.fieldSize = fieldSize;
    this.updatableSubSize = fieldSize / (concurrencyLevel * 2);
    this.updaters = new RecursiveTask[concurrencyLevel * 2];
  }

  @Override
  public long update(Field field) {
    field.nextStep();
    long cellCount = 0;
    for (int i = 0; i < updaters.length; i++) {
      this.updaters[i] = new UpdateTask(i, field);
    }
    for (int i = 0; i < updaters.length; i+=2) {
      updaters[i].fork();
    }
    for (int i = 0; i < updaters.length; i+=2) {
      cellCount += updaters[i].join();
    }
    for (int i = 1; i < updaters.length; i+=2) {
      updaters[i].fork();
    }
    for (int i = 1; i < updaters.length; i+=2) {
      cellCount += updaters[i].join();
    }
    return cellCount;
  }

  private class UpdateTask extends RecursiveTask<Long> {

    private final int index;
    private final Field field;

    private UpdateTask(int index, Field field) {
      this.index = index;
      this.field = field;
    }

    @Override
    protected Long compute() {
      return field.update(index * updatableSubSize, ((index + 1) * updatableSubSize) - 1, 0, fieldSize - 1);
    }
  }
}
