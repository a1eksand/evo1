package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Objects;
import java.util.function.Supplier;
import javax.swing.JComponent;
import orchestration.Orchestrator;

public class Canvas extends JComponent implements MouseMotionListener, MouseListener, MouseWheelListener, Orchestrator.Renderer {

  private final static int UI_HEIGHT = 12;
  private final static int UI_WIDTH = 60;
  private final double[] stats = new double[STATS_COUNT];
  private volatile int scale = 10;
  private volatile int cx = 0;
  private volatile int cy = 0;
  private long time2;
  private long time3;
  private Supplier<Color>[][] state;
  private MouseEvent mousePressed1;

  public Canvas() {
    super();
    this.addMouseMotionListener(this);
    this.addMouseListener(this);
    this.addMouseWheelListener(this);
  }

  @Override
  public void paint(Graphics g) {
    var g2D = (Graphics2D) g;
    paintBackground(g2D);
    paintWord(g2D);
    paintUI(g2D);
    g2D.dispose();
  }

  private void paintBackground(Graphics2D g) {
    g.setPaint(Color.black);
    g.fillRect(0, 0, getWidth(), getHeight());
  }

  private void paintWord(Graphics2D g) {
    int scale = this.scale;
    if (Objects.isNull(state)) {
      return;
    }
    for (int x = 0; x < state.length; x++) {
      for (int y = 0; y < state[x].length; y++) {
        if (Objects.nonNull(state[x][y])) {
          var colorSupplier = state[x][y];
          if (Objects.nonNull(colorSupplier)) {
            int x1 = x * scale + cx;
            int y1 = y * scale + cy;
            if (x1 + scale >= 0 && x1 < getWidth() && y1 + scale >= 0 && y1 < getHeight()) {
              g.setPaint(colorSupplier.get());
              g.fillRect(x1, y1, scale, scale);
            }
          }
        }
      }
    }
  }

  private void paintUI(Graphics2D g) {
    long time1 = System.currentTimeMillis();
    int line = 1;
    g.setPaint(Color.white);
    g.drawString("%.2f".formatted(1_000. / (time1 - time3)), getWidth() - UI_WIDTH, line++ * UI_HEIGHT);
    g.drawString("%.2f".formatted(stats[IPS]), getWidth() - UI_WIDTH, line++ * UI_HEIGHT);
    g.drawString("%s".formatted(formatCount(stats[IPS] * stats[CELL_COUNT])), getWidth() - UI_WIDTH, line++ * UI_HEIGHT);
    g.drawString("x: %d".formatted(cx), getWidth() - UI_WIDTH, line++ * UI_HEIGHT);
    g.drawString("y: %d".formatted(cy), getWidth() - UI_WIDTH, line++ * UI_HEIGHT);
    g.drawString("s: %d".formatted(scale), getWidth() - UI_WIDTH, line++ * UI_HEIGHT);
    g.drawString("ST: %d".formatted((long) stats[STEP]), getWidth() - UI_WIDTH, line++ * UI_HEIGHT);
    g.drawString("CC: %s".formatted(formatCount((long) stats[CELL_COUNT])), getWidth() - UI_WIDTH, line++ * UI_HEIGHT);
    time3 = time1;
  }

  private String formatCount(long count) {
    if (count > 1_000_000) {
      return count / 1_000_000 + "M";
    }
    if (count > 1_000) {
      return count / 1_000 + "k";
    }
    return String.valueOf(count);
  }

  private String formatCount(double count) {
    if (count > 1_000_000) {
      return "%.2fM".formatted(count / 1_000_000);
    }
    if (count > 1_000) {
      return "%.2fk".formatted(count / 1_000);
    }
    return "%.2f".formatted(count);
  }


  @Override
  public void mouseDragged(MouseEvent e) {

  }

  @Override
  public void mouseMoved(MouseEvent e) {

  }

  @Override
  public void mouseClicked(MouseEvent e) {
    int ex = e.getX();
    int ey = e.getY();
    int scale = this.scale;
    if (Objects.isNull(state)) {
      return;
    }

    for (int x = 0; x < state.length; x++) {
      for (int y = 0; y < state[x].length; y++) {
        if (Objects.nonNull(state[x][y])) {
          var colorSupplier = state[x][y];
          if (Objects.nonNull(colorSupplier)) {
            int x1 = x * scale + cx;
            int y1 = y * scale + cy;
            if (x1 + scale >= 0 && x1 < getWidth() && y1 + scale >= 0 && y1 < getHeight()) {
              if (x1 <= ex && ex <= (x1 + scale) && y1 <= ey && ey <= (y1 + scale)) {
                System.err.println(colorSupplier);
                return;
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.getButton() == 1) {
      mousePressed1 = e;
    }
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (e.getButton() == 1 && Objects.nonNull(mousePressed1)) {
      cx += (e.getX() - mousePressed1.getX());
      cy += (e.getY() - mousePressed1.getY());
      mousePressed1 = null;
    }
  }

  @Override
  public void mouseEntered(MouseEvent e) {

  }

  @Override
  public void mouseExited(MouseEvent e) {

  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    scale = Math.max(1, scale - e.getWheelRotation());
  }

  @Override
  public void render(Supplier<Color>[][] state, double[] stats) {
    long time1 = System.currentTimeMillis();
    this.state = state;
    if (time1 - time2 > 1_000) {
      System.arraycopy(stats, 0, this.stats, 0, stats.length);
      time2 = time1;
    }
    repaint();
  }
}
