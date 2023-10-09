package orchestration;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import universe.Field;

public class Orchestrator {

  protected final Field field;
  protected final Updater updater;
  protected final Thread updateExecutor;
  protected final Thread renderExecutor;
  protected volatile int fps = 30;
  protected volatile int ips = 60;
  protected volatile boolean isStopped = false;
  protected volatile boolean isPaused = true;
  protected final double[] wsb1 = new double[Renderer.STATS_COUNT];
  protected final double[] wsb2 = new double[Renderer.STATS_COUNT];
  protected long time3;

  public Orchestrator(Field field, Renderer renderer, Updater updater) {
    this.field = field;
    this.updater = updater;
    updateExecutor = new Thread(() -> {
      while (!isStopped) {
        while (isPaused) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            return;
          }
        }
        long time1 = System.currentTimeMillis();
        wsb1[Renderer.CELL_COUNT] = update();
        wsb1[Renderer.IPS] = 1_000. / (time1 - time3);
        System.arraycopy(wsb1, 0, wsb2, 0, wsb1.length);
        try {
          Thread.sleep(Math.max(0L, 1_000L / ips - (System.currentTimeMillis() - time1)));
        } catch (InterruptedException e) {
          return;
        }
        time3 = time1;
      }
      updater.stop();
    });

    renderExecutor = new Thread(() -> {
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

  public void start() {
    updateExecutor.start();
    renderExecutor.start();
  }

  public void stop() {
    isStopped = true;
  }

  public void pause() {
    isPaused = true;
    System.gc();
  }

  public void resume() {
    isPaused = false;
  }

  long update() {
    return updater.update(field);
  }

  public Field getField() {
    return field;
  }

  public List<String> setVar(String varName, String value) {
    try {
      field.setVar(Field.Var.valueOf(varName), Integer.parseInt(value));
    } catch (IllegalArgumentException e) {
      return List.of("%s: %s".formatted(e.getClass().getSimpleName(), e.getMessage()));
    }
    int[] vars = field.getVars();
    return Arrays.stream(Field.Var.values()).map(var -> "%s: %d".formatted(var, vars[var.index])).toList();
  }

  public List<String> getVars() {
    int[] vars = field.getVars();
    return Arrays.stream(Field.Var.values()).map(var -> "%s: %d".formatted(var, vars[var.index])).toList();
  }

  public void setFps(int fps) {
    this.fps = fps;
  }

  public void setIps(int ips) {
    this.ips = ips;
  }

  public interface Renderer {
    int STATS_COUNT = 3;
    int CELL_COUNT = 0;
    int IPS = 1;
    int STEP = 2;

    void render(Supplier<Color>[][] state, double[] stats);
  }

  interface Updater {

    default Updater init(Supplier<RandomGenerator> rnd) {
      return this;
    }

    long update(Field field);

    default void stop() {
    }
  }
}
