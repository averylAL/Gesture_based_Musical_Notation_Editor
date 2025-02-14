package sandbox;

import music.UC;
import graphics.WinApp;
import java.awt.Color;
import java.awt.Graphics;

public class RedRect extends WinApp{
  public static final int W = UC.mainWindowWidth;
  public static final int H = UC.mainWindowHeight;
  public RedRect(){super("Red Rect", W, H );} // Win Title, Win width, Win height

  @Override
  public void paintComponent(Graphics g){  // called by OS whenever it needs to show this window
    // Override this method, @override is like a comment
    g.setColor(Color.RED);         // use the color red
    g.fillRect(100,100,100,100);   // to fill in a rectangle
  }

  public static void main(String[] args){
    PANEL = new RedRect();  // PANEL is where the paintComponent code lives, static element
    WinApp.launch();        // fire up the WinApp thread the OS manages
  }

}
