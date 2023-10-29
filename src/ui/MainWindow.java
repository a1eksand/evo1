package ui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import javax.swing.JFrame;
import universe.Field;
import utils.Logger;

public class MainWindow extends JFrame {

  static private final String HELP = """
      stop - pause simulation
      start - resume simulation
      save [PREFIX] - save simulation to file
      load FILE - load simulation from file
      logger [CLASS LEVEL] - show / set logging level
      """;

  public static void main(String[] args) throws IOException {
    var window = new MainWindow();
    window.setVisible(true);
    window.setTitle("EVO1");
    window.setSize(1600, 900);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setLayout(new BorderLayout());
    var canvas = new Canvas();
    window.add(canvas);

    var orc = new Orchestrator(Field.rnd(Field.SEED, 1000, 10000), canvas);
    orc.start();

    var input = new Scanner(System.in);
    System.out.print(HELP);
    while (input.hasNextLine()) {
      var cmd = input.nextLine().trim().split("\\s+");
      switch (cmd[0].toLowerCase()) {
        case "stop" -> orc.pause();
        case "start" -> orc.resume();
        case "save" -> orc.serialize(Files.newOutputStream(Path.of((cmd.length > 1 ? cmd[1] : "") + System.currentTimeMillis() + ".state")));
        case "load" -> {
          orc.stop();
          orc = new Orchestrator(Field.load(Files.newInputStream(Path.of(cmd[1]))), canvas);
          orc.start();
        }
        case "logger" -> {
          if (cmd.length > 1) {
            Logger.Factory.setLevel(cmd[1], cmd[2]);
          } else {
            System.out.println(String.join("\n", Logger.Factory.getLevels()));
          }
        }
        default -> System.out.print(HELP);
      }
    }
  }
}
