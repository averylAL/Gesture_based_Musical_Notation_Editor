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
  public static final int noBid = 100000;
  public static final int barToMarginSnap = 20;
  public static final int minStaffGap = 8;
  public static final int minSysGap = 8;
  public static final int snapTime = 30;
  public static final int restFirstDotOffset = 28;
  public static final int dotSpacingRest = 11;
  public static final int initialClefOffset = 30;
  public static final int defaultStaffH = 8;
  public static final int marginKeyOffset = 60;
  public static final int barKeyOffset = 40;

  //-------------------- Color ----------------------------
  public static final int largestPossibleCoordinate = 5000;
  public static Color inkColor = Color.CYAN;
  public static final int normSampleSize = 25;
  public static final int dotThreshold = 5;
  public static String shapeDatabaseFileName = "shapeDB.dat";
  public static String FontName = "Sinfonia";
}
