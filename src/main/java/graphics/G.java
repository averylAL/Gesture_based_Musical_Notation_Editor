package graphics;

import java.awt.Color;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.Random;

/**
 * Helper Class
 */
public class G implements Serializable {
  public static void spline(Graphics g, int ax, int ay, int bx, int by, int cx, int cy, int n){
    if(n == 0){g.drawLine(ax, ay, cx, cy);return;}
    int abx = (ax + bx) / 2, aby = (ay + by) / 2;
    int bcx = (bx + cx) / 2, bcy = (by + cy) /2;
    int abcx = (abx + bcx) /2, abcy = (aby + bcy) /2;
    spline(g,ax,ay,abx,aby,abcx,abcy,n-1);
    spline(g,abcx,abcy,bcx,bcy,cx,cy,n-1);
  };
  public static Random RND = new Random();
  public static int rnd (int max) { return RND.nextInt(max); }
  public static Color rndColor() { return new Color(rnd(256), rnd(256), rnd(256)); }
  public static void clearScreen(Graphics g) {
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, 5000, 5000);
  }
  // ------------------ V --------------------
  public static class V implements Serializable {
    public static Transform T = new Transform();
    public int x, y;
    public V(int x, int y) {set(x, y);}
    public void set(int x, int y) {this.x = x; this. y = y;}
    public void add(V v) { x += v.x; y += v.y;}
    public void set(V v) {set(v.x, v.y);}
    public void setT(V v) {set(v.tx(), v.ty());}
    public int tx() { return x * T.n / T.d + T.dx;}
    public int ty() { return y * T.n / T.d + T.dy;}
    public void blend(V v, int k) {set((k*x + v.x) / (k + 1), (k*y + v.y) / (k + 1));}
    // ----------------- Transform -----------------
    public static class Transform{
      int dx, dy, n, d; // how much sliding; friction n/d; (x',y') = (x*n/d + dx, y*n/d + dy)
      private void setScale(int oW, int oH, int nW, int nH) {
        n = (nW > nH) ? nW :nH;
        d = (oW > oH) ? oW :oH;
      }
      private int setOff(int oX, int oW, int nX, int nW) {
        return (-oX - oW/2)*n/d + nX + nW/2;
      }
      public void set(VS oVS, VS nVS) {
        setScale(oVS.size.x, oVS.size.y, nVS.size.x, nVS.size.y);
        dx = setOff(oVS.loc.x, oVS.size.x, nVS.loc.x, nVS.size.x);
        dy = setOff(oVS.loc.y, oVS.size.y, nVS.loc.y, nVS.size.y);
      }
      public void set(BBox from, VS to) {
        setScale(from.h.size(), from.v.size(), to.size.x, to.size.y);
        dx = setOff(from.h.lo, from.h.size(), to.loc.x, to.size.x);
        dy = setOff(from.v.lo, from.v.size(), to.loc.y, to.size.y);
      }


    }
  }

  // ------------------ VS --------------------
  public static class VS {
    public V loc, size;
    public VS(int x, int y, int w, int h) {loc = new V(x, y); size = new V(w, h);}
    public void fill(Graphics g, Color c) {
      g.setColor(c);
      g.fillRect(loc.x, loc.y, size.x, size.y);
    }
    public boolean hit(int x, int y) {
      return loc.x <= x && loc.y <= y && x <= loc.x + size.x && y <= loc.y + size.y;
    }
    public int xL() {return loc.x;}
    public int xH() {return loc.x + size.x;}
    public int xM() {return loc.x + size.x / 2;}
    public int yL() {return loc.y;}
    public int yH() {return loc.y + size.y;}
    public int yM() {return loc.y + size.y / 2;}
  }

  // ------------------ LoHi --------------------
  public static class LoHi {
    public int lo, hi;
    public LoHi(int min, int max) {lo = min; hi = max;}
    public void set(int val){lo = val; hi = val;}
    public void add(int val){
      if (val < lo) {lo = val;}
      if (val > hi) {hi = val;}
    }
    public int size(){return (hi - lo) > 0 ? (hi - lo) : 1;} // if true return (hi-lo) or 1
  }

  // ------------------ BBox --------------------
  public static class BBox {
    public LoHi h, v;
    public BBox() {h = new LoHi(0, 0); v = new LoHi(0, 0);}
    public void set(int x, int y){h.set(x); v.set(y);} // give box surround that figure
    public void add(int x, int y){h.add(x); v.add(y);}
    public void add(V vec){h.add(vec.x); v.add(vec.y);}
    public VS getNewVS() {return new VS(h.lo, v.lo, h.hi - h.lo, v.hi - v.lo);} // take existing bounding box into vs
    public void draw(Graphics g) {g.drawRect(h.lo, v.lo, h.hi - h.lo, v.hi - v.lo);}

  }

  // ------------------ PL --------------------
  public static class PL implements Serializable {
    public V[] points;
    public PL(int count) {
      points = new V[count];
      for (int i = 0; i < count; i++) {points[i] = new V(0, 0);}
    }
    public int size() {return points.length;}
    public void drawNDots(Graphics g, int n) {
      g.setColor(Color.BLUE);
      for (int i = 0; i < n; i++) {
        g.drawOval(points[i].x - 2, points[i].y - 2, 4, 4); // make a circle, radius is 2
      }
    }
    public void drawN(Graphics g, int n) {
      for (int i = 1; i < n; i++) {
        g.drawLine(points[i - 1].x, points[i - 1].y, points[i].x, points[i].y );
      }
      drawNDots(g, n);
    }

    public void draw(Graphics g) {drawN(g, points.length);}
    public void transform() {for (int i = 0; i < points.length; i++) {points[i].setT(points[i]);}}

  }

  //-----------------------Hierarchical Coordinate----------------------------------------
  public static class HC{
    public static HC ZERO = new HC(null, 0);

    public HC dad;
    public int dv;// delta value from dad

    public HC(HC dad, int dv){this.dad = dad; this.dv=dv;}
    public int v(){return dad == ZERO ? dv : dad.v()+dv;} // value of coordinate
  }

}
