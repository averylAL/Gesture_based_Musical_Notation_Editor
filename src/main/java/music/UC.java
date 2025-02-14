package music;

import java.awt.Color;

/**
 * Universal constants for entire project
 */
public class UC {
  public static final int mainWindowWidth = 1000;
  public static final int mainWindowHeight = 1000;
  public static final int inkBufferMax = 500;
  public static final int normCoordMax = 1000;
  public static final int noMatchDist = 500000;

  //-------------------- Color ----------------------------
  public static final int largestPossibleCoordinate = 5000;
  public static Color inkColor = Color.CYAN;
  public static final int normSampleSize = 25;
  public static final int dotThreshold = 5;
  public static String shapeDatabaseFileName = "shapeDB.dat";
}
