package sandbox;
import graphics.G;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import graphics.WinApp;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Paint extends WinApp {
  // static members
  // non-static members
  // constructors
  // non-static functions
  // static functions
  public static int clicks = 0;
  public static Path thePath = new Path();
  public static Pic thePic = new Pic();

  public Paint() {
    super("Paint", 1000, 700);
  }

  @Override
  public void paintComponent(Graphics g) {
    G.clearScreen(g);
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, 5000, 5000);
    g.setColor(G.rndColor());
    g.fillOval(100, 100, 100, 200);
//    g.drawOval(100, 100, 100, 200);
    g.setColor(Color.BLACK);
    g.drawLine(100, 600, 600, 100);
    int x = 400, y = 200;
    g.setFont(new Font("TimesRoman", Font.PLAIN, 28));
    String msg = "Dude " + clicks;
    g.drawString(msg, x, y);
    g.setColor(Color.RED);
    g.fillOval(x, y, 3, 3);
    FontMetrics fm = g.getFontMetrics();
    int a = fm.getAscent(); // distance between baseline and the font, above
    int d = fm.getDescent();
    int w = fm.stringWidth(msg);
    g.drawRect(x, y - a, w, a + d);

    int l = fm.getLeading(); // how far before draw next line, default leading is 0
//    System.out.println("Leading " + l);
    g.setColor(Color.BLUE);
    g.drawRect(x, y - a, w, a + d + l);

//    g.clearRect(0, 0, 150, 150);
//    thePath.draw(g);
    thePic.draw(g);
  }

  @Override
  public void mousePressed(MouseEvent me) {
    clicks++;
//    thePath.clear();
    thePath = new Path();
    thePic.add(thePath);
    thePath.add(me.getPoint());
    repaint();
  }

  @Override
  public void mouseDragged(MouseEvent me) {
    thePath.add(me.getPoint());
    repaint();
  }

  public static void main(String[] args) {
    PANEL = new Paint();
    WinApp.launch();
  }

  // ------------------ path ----------------------
  // write nested classes below
  public static class Path extends ArrayList<Point> {
    public void draw(Graphics g) {
      for (int i = 1; i < size(); i++) {
        Point p = get(i - 1), n = get(i);
        g.drawLine(p.x, p.y, n.x, n.y);
      }
    }
  }

  public static class Pic extends ArrayList<Path> {
    public void draw(Graphics g) { for (Path path : this) { path.draw(g); } }
  }

}
