package sandbox;

import graphics.WinApp;
import java.awt.Color;
import java.awt.Graphics;

public class BlueRect extends WinApp {
  public BlueRect () {
    super("Blue Rect", 1000, 700);
  }
  public void paintComponent(Graphics g) {
    g.setColor(Color.BLUE);
    g.drawRect(100, 100, 100, 100);
  }
  public static void main (String[] args) {
    PANEL = new BlueRect();
    WinApp.launch();
  }

}
