package music;

import reactions.*;

import java.awt.Graphics;

public interface I {
  public interface Show { public void show(Graphics g); }
  public interface Hit { public boolean hit(int x, int y); }
  public interface Area extends Hit{
    public void dn (int x, int y); // down
    public void up (int x, int y);
    public void drag (int x, int y);
  }
  public interface Act {public void act(Gesture g);}
  public interface React extends Act{public int bid(Gesture g);}

}
