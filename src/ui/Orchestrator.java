package ui;

import java.awt.Color;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;
import universe.Field;

public class Orchestrator {

  private final Field field;
  private final Thread executor1;
  private final Thread executor2;
  private volatile int fps = 60;
  private volatile int ips = 60;
  private volatile boolean isStopped = false;
  private volatile boolean isPaused = true;
  private final double[] wsb1 = new double[Renderer.STATS_COUNT];
  private final double[] wsb2 = new double[Renderer.STATS_COUNT];
  private long time3;

  Orchestrator(Field field, Renderer renderer) {
    this.field = field;
    RecursiveTask<Integer>[] updaters = new RecursiveTask[Field.CONCURRENT_LEVEL];
    executor1 = new Thread(() -> {
      while (!isStopped) {
        while (isPaused) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            return;
          }
        }
        long time1 = System.currentTimeMillis();
        wsb1[Renderer.CELL_COUNT] = 0;
        field.updaters(updaters);
        for (int j = 0; j < updaters.length; j++) {
          updaters[j].fork();
        }
        for (int j = 0; j < updaters.length; j++) {
          wsb1[Renderer.CELL_COUNT] += updaters[j].join();
        }
        wsb1[Renderer.IPS] = 1_000. / (time1 - time3);
        System.arraycopy(wsb1, 0, wsb2, 0 ,wsb1.length);
        try {
          Thread.sleep(Math.max(0L, 1_000L / ips - (System.currentTimeMillis() - time1)));
        } catch (InterruptedException e) {
          return;
        }
        time3 = time1;
      }
    });

    executor2 = new Thread(() -> {
      while (!isStopped) {
        long time1 = System.currentTimeMillis();
        wsb2[Renderer.STEP] = field.getStep();
        renderer.render(field.getState(), wsb2);
        try {
          Thread.sleep(Math.max(0L, 1_000L / fps - (System.currentTimeMillis() - time1)));
        } catch (InterruptedException e) {
          return;
        }
      }
    });
  }

  void start() {
    executor1.start();
    executor2.start();
  }

  void stop() {
    isStopped = true;
  }

  void pause() {
    isPaused = true;
  }
  void resume() {
    isPaused = false;
  }

  Field getField() {
    return field;
  }

  interface Renderer {
    int STATS_COUNT = 3;
    int CELL_COUNT = 0;
    int IPS = 1;
    int STEP = 2;

    void render(Supplier<Color>[][] state, double[] stats);
  }
}
