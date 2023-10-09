package orchestration;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;
import universe.Field;
import utils.TLRandom;

public class OrchestratorTest {

  final static int FIELD_SIZE = 512;
  final static int CCC = 4;
  final static long STEP_TO_STOP = 400;
  static long startTestTime;
  static long endTestTime;

  public static void main(String[] args) throws IOException {
    TLRandom rnd;
    TestableOrchestrator orc;
    orc = new TestableOrchestrator(Field.rnd(rnd = new TLRandom(73), FIELD_SIZE, 20 * FIELD_SIZE), new TestableRenderer(), new SeveralSingleThreadExecutorUpdater(FIELD_SIZE, CCC).init(rnd));
    orc.setFps(1);
    orc.start();
    startTestTime = System.currentTimeMillis();
    orc.resume();

    wait(orc);

    System.out.printf("%5.2f c/ms\n", 1.0 * orc.totalUpdatedCellCount / (endTestTime - startTestTime));
    orc.getField().snapshot(Files.newBufferedWriter(Path.of(("OrchestratorTest1.csv"))));

    orc = null;
    System.gc();

    orc = new TestableOrchestrator(Field.load(Files.newBufferedReader(Path.of("OrchestratorTest1.csv")), rnd = new TLRandom(Field.SEED), Field.SIZE), new TestableRenderer(), new SeveralSingleThreadExecutorUpdater(Field.SIZE, CCC).init(rnd));
    orc.setFps(1);
    orc.start();
    startTestTime = System.currentTimeMillis();
    orc.resume();

    wait(orc);

    System.out.printf("%5.2f c/ms\n", 1.0 * orc.totalUpdatedCellCount / (endTestTime - startTestTime));
    orc.getField().snapshot(Files.newBufferedWriter(Path.of(("OrchestratorTest2.csv"))));
  }

  static void wait(TestableOrchestrator orc) {
    while (!orc.isStopped()) {
      try {
        System.out.printf("%5.2f%%\n", 1.0 * orc.getField().getStep() / STEP_TO_STOP * 100);
        Thread.sleep(1000L);
      } catch (InterruptedException e) {
        return;
      }
    }
  }

  static class TestableOrchestrator extends Orchestrator {

    long totalUpdatedCellCount = 0;

    TestableOrchestrator(Field field, Renderer renderer, Orchestrator.Updater updater) {
      super(field, renderer, updater);
    }

    @Override
    protected long update() {
      long cellCount = updater.update(field);
      totalUpdatedCellCount += cellCount;
      if (field.getStep() == STEP_TO_STOP) {
        stop();
        endTestTime = System.currentTimeMillis();
      }
      return cellCount;
    }

    boolean isStopped() {
      return isStopped;
    }
  }

  static class TestableRenderer implements Orchestrator.Renderer {

    @Override
    public void render(Supplier<Color>[][] state, double[] stats) {

    }
  }
}

