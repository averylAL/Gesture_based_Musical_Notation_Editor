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
  public static final int noMatchDist = 500_000;
  public static final int noBid = 10_000; // can 10000 or 10_000
  public static final int minStaffGap = 40;
  public static final int minSysGap =50;
  public static final int barToMarginSnap = 20;
  public static final String FontName = "sinfonia";
  public static final int snapTime = 30;
  public static final int AugDotSpacing = 11;
  public static final int AugDotOffSet = 28;
  public static final int initialClefOffset = 30;
  public static final int marginKeyOffset = 60;
  public static final int barKeyOffset = 10;
  public static final int accidentalHeadGap = 30;

  //-------------------- Color ----------------------------
  public static final int largestPossibleCoordinate = 5000;
  public static Color inkColor = Color.CYAN;
  public static final int normSampleSize = 25;
  public static final int dotThreshold = 5;
  public static String shapeDatabaseFileName = "shapeDB.dat";
}
