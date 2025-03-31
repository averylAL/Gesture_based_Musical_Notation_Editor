package sandbox;

import graphics.G;
import graphics.G.VS;
import graphics.WinApp;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.Timer;
import music.I;
import music.UC;

public class Squares extends WinApp implements ActionListener {
  public static Color color = G.rndColor();
  public static G.VS theVS = new VS(100, 100, 200, 300);
  public static Square.List squares = new Square.List();
  public static Square lastSquare;
  public static boolean dragging = false;
  public static boolean showSpline = false;
  public static G.V mouseDelta = new G.V(0, 0);
  public static Timer timer;
  public static G.V pressedLoc = new G.V(0, 0);
  public Squares() {
    super("Squares", 1000, 700);
    timer = new Timer(30, this);
    timer.setInitialDelay(5000);
    timer.start();
  }
  public void paintComponent(Graphics g) {
    G.clearScreen(g);
//    theVS.fill(g, color);
    squares.draw(g);
    if(showSpline && squares.size() >= 2) {
      g.setColor(Color.BLACK);
      G.V a = squares.get(0).loc, b = squares.get(1).loc, c = squares.get(2).loc;
      G.spline(g, a.x, a.y, b.x, b.y, c.x, c.y, 4);
    }
  }

  public static I.Area curArea; // the area that handle mouse event
  public void mousePressed(MouseEvent me) {
    int x = me.getX(), y = me.getY();
    curArea = squares.hit(x, y); // when mouse hit, here's the area
    curArea.dn(x, y);
    repaint();
  }

  public void mouseDragged(MouseEvent me) {
    curArea.drag(me.getX(), me.getY());
    repaint();
  }

  public void mouseReleased(MouseEvent me) {
    curArea.up(me.getX(), me.getY());
    repaint();
  }

  //  public void mousePressed(MouseEvent me) {
//    int x = me.getX(), y = me.getY();
////    if (theVS.hit(x, y)) {color = G.rndColor();}
//    lastSquare = squares.hit(x, y);
//    // if click background -> new square
//    if(lastSquare == null) {
//      dragging = false;
//      lastSquare = new Square(x, y);
//      squares.add(lastSquare);
//    } else {
//      mouseDelta.set(lastSquare.loc.x -x, lastSquare.loc.y - y);
//      dragging = true;
//      lastSquare.dv.set(0, 0);
//      pressedLoc.set(x, y);
//
//    }
//    repaint();
//  }

//  public void mouseDragged(MouseEvent me) {
//    int x = me.getX(), y = me.getY();
//    if(dragging) {
//      lastSquare.moveTo(x + mouseDelta.x, y+mouseDelta.y);
//    } else {
//      lastSquare.resize(x, y);
//    }
//    repaint();
//  }
  public static void main(String[] args) {
    PANEL = new Squares();
    WinApp.launch();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    repaint();
  }

//  @Override
//  public void mouseReleased(MouseEvent me){
//    if(dragging){
//      lastSquare.dv.set(me.getX() - pressedLoc.x, me.getY() - pressedLoc.y);
//    }
//  }

  // ------------------ Square ---------------------
  public static class Square extends G.VS implements I.Area{
    /**
     * Anonymous Class
     */
    public static Square BACKGROUND = new Square() {
      public void dn (int x, int y) {lastSquare = new Square(x, y); squares.add(lastSquare);}
      public void drag(int x, int y) {lastSquare.resize(x, y);}
    };
    public Color c = G.rndColor();
//    public G.V  dv = new G.V(G.rnd(20) - 10, G.rnd(20) - 10);
    public G.V  dv = new G.V(0, 0); // stop the animation, none of them are moving
    public Square(int x, int y) {super(x, y, 100, 100);}
    public Square() {
      super(0, 0, UC.largestPossibleCoordinate, UC.largestPossibleCoordinate);
      c = Color.WHITE;
    }
    public void draw(Graphics g) {fill(g, c); moveAndBounce();}
    public void resize(int x, int y) {
      if (x > loc.x && y > loc.y) {size.set(x-loc.x, y-loc.y);}
    }
    public void moveTo(int x, int y) {loc.set(x, y);}
    public void moveAndBounce() {
      loc.add(dv);
      // check if hit boundaries
      if(xL() < 0 && dv.x <0) {dv.x = -dv.x;}
      if(yL() < 0 && dv.y <0) {dv.y = -dv.y;}
      if(xH() > 1000 && dv.x >0) {dv.x = -dv.x;}
      if(yH() > 700 && dv.y >0) {dv.y = -dv.y;}
    }

    @Override
    public void dn(int x, int y) { mouseDelta.set(loc.x - x, loc.y - y); }

    @Override
    public void up(int x, int y) {}

    @Override
    public void drag(int x, int y) { loc.set(mouseDelta.x + x, mouseDelta.y + y); }

    // -------------------- List ---------------------
    public static class List extends ArrayList<Square> {
      public List() {
        super();
        this.add(Square.BACKGROUND);
      }
      public void draw(Graphics g) {for (Square s : this) {s.draw(g);}}
        public Square hit(int x, int y){
          Square res = null;
          for(Square s: this){if(s.hit(x,y)){res = s;}}
          return res;
        }
    }
  }

}
