package ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import javax.swing.JFrame;
import orchestration.SeveralSingleThreadExecutorUpdater;
import orchestration.Orchestrator;
import universe.Field;
import utils.Logger;
import utils.TLRandom;

public class MainWindow extends JFrame {

  static final int CMD_INDEX = 0;
  static final int CONCURRENCY_LEVEL = 4;

  static private final String HELP = """
      stop - pause simulation
      start - resume simulation
      save [PREFIX] - save simulation to jsd file
      snap [PREFIX] - save simulation to csv file
      load FILE - load simulation from file
      logger [CLASS LEVEL] - show / set logging level
      vars ACTION_COST_MOVE 10
      """;

  public static void main(String[] args) throws IOException {
    TLRandom rnd;

    var window = new MainWindow();
    window.setVisible(true);
    window.setTitle("EVO1");
    window.setSize(1600, 900);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setLayout(new BorderLayout());
    var canvas = new Canvas();
    window.add(canvas);

    var orc = new Orchestrator(Field.create(rnd = new TLRandom(Field.SEED), Field.SIZE, 10 * Field.SIZE), canvas, new SeveralSingleThreadExecutorUpdater(Field.SIZE, CONCURRENCY_LEVEL).init(rnd));
    orc.start();

    var input = new Scanner(System.in);
    System.out.print(HELP);
    System.out.print("> ");
    while (input.hasNextLine()) {
      var cmd = input.nextLine().trim().split("\\s+");
      switch (cmd[CMD_INDEX].toLowerCase()) {
        case "stop" -> orc.pause();
        case "start" -> orc.resume();
        case "save" -> orc.getField().serialize(Files.newOutputStream(Path.of((cmd.length > CMD_INDEX + 1 ? cmd[CMD_INDEX + 1] : "") + System.currentTimeMillis() + ".state.jsd")));
        case "snap" -> orc.getField().snapshot(Files.newBufferedWriter(Path.of((cmd.length > CMD_INDEX + 1 ? cmd[CMD_INDEX + 1] : "") + System.currentTimeMillis() + ".state.csv")));
        case "load" -> {
          orc.stop();
          var file = cmd[CMD_INDEX + 1];
          if (file.contains(".state.jsd")) {
            orc = null;
            System.gc();
            orc = new Orchestrator(Field.create(Files.newInputStream(Path.of(cmd[CMD_INDEX + 1]))), canvas, new SeveralSingleThreadExecutorUpdater(Field.SIZE, CONCURRENCY_LEVEL));
          } else if (file.contains(".state.csv")) {
            orc = null;
            System.gc();
            orc = new Orchestrator(Field.create(Files.newBufferedReader(Path.of(cmd[CMD_INDEX + 1])), rnd = new TLRandom(Field.SEED), Field.SIZE), canvas, new SeveralSingleThreadExecutorUpdater(Field.SIZE, CONCURRENCY_LEVEL).init(rnd));
          } else {
            System.out.println("Unknown file type.");
            orc = new Orchestrator(orc.getField(), canvas, new SeveralSingleThreadExecutorUpdater(Field.SIZE, CONCURRENCY_LEVEL));
          }
          orc.start();
        }
        case "logger" -> {
          if (cmd.length > CMD_INDEX + 1) {
            Logger.Factory.setLevel(cmd[CMD_INDEX + 1], cmd[CMD_INDEX + 2]);
          } else {
            System.out.println(String.join("\n", Logger.Factory.getLevels()));
          }
        }
        case "vars" -> {
          if (cmd.length > CMD_INDEX + 1) {
            System.out.println(String.join("\n", orc.setVar(cmd[CMD_INDEX + 1], cmd[CMD_INDEX + 2])));
          } else {
            System.out.println(String.join("\n", orc.getVars()));
          }
        }
        default -> System.out.print(HELP);
      }
      System.out.print("> ");
    }
  }
}
