package ui;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import universe.Field;

public class MainWindow extends JFrame {

  public static void main(String[] args) {
    var window = new MainWindow();
    window.setVisible(true);
    window.setTitle("EVO1");
    window.setSize(1600, 900);
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.setLayout(new BorderLayout());
    var canvas = new Canvas();
    window.add(canvas);
    new Orchestrator(Field.rnd(Field.SEED, 1000, 10000), canvas).start();
  }
}
