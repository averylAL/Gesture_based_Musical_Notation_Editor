package music;

import reactions.*;

import java.awt.Graphics;
import java.util.ArrayList;

public interface I {
  public interface Show { public void show(Graphics g); }
  public interface Hit { public boolean hit(int x, int y); }
  public interface Area extends Hit{
    public void dn (int x, int y); // down
    public void up (int x, int y);
    public void drag (int x, int y);
  }
  public interface Act{public void act(Gesture g);} // what you do if you react to some gesture
  public interface React extends Act{public int bid(Gesture g);} // how badly you want to so something

  public interface Margins{
    public int top();
    public int left();
    public int right();
    public int bot();
  }
  public interface Page extends Margins{
    public Sys.Fmt sysfmt();
    public ArrayList<Sys> systems();
  }
  public interface MusicApp{
    public ArrayList<Page> pages();
    public Sys.Fmt sysfmt(I.Page page);
    public ArrayList<Sys> systems(I.Page page);
  }
}
