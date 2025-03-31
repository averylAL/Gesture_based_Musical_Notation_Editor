package music;

import graphics.G;

import java.awt.*;

public class Key {
    public static int[] //line# for sharps and flats in G-clef and F-clef
    sG = {0, 3, -1, 2, 5, 1, 4},
    fG = {4, 1, 5, 2, 6, 3, 7},
    sF = {2, 5, 1, 4, 7, 3, 6},
    fF = {6, 3, 7, 4, 8, 5, 9};

    public int n = 0;//range from -7 to 7 for flats and sharps
    public Glyph glyph = Glyph.SHARP; //flat, sharp or natural

    public static void drawOnStaff(Graphics g, int n, int[] lines, int x, Glyph glyph, Staff staff) {
        int gap = gapForGlyph(glyph, staff);
        for (int i = 0; i < n; i++) {
            glyph.showAt(g, staff.fmt.H, x + i*gap, staff.yOfLine(lines[i]));
        }
    }

    public void drawOnSys(Graphics g, Sys sys, int x){
        for(Staff staff: sys.staffs){
            if(n == 0){ return; } // nothing to draw
            int[] arr;
            boolean isG = staff.clefAtX(x) == Glyph.CLEF_G;
            if(n > 0){//we have Sharps
                arr = isG ? sG:sF;
            }else {
                arr = isG ? fG:fF;
            }
            drawOnStaff(g, Math.abs(n), arr ,x, glyph, staff);
        }
    }

    public static int gapForGlyph(Glyph glyph, Staff staff) {
        int h = staff.fmt.H;
        if(glyph == Glyph.SHARP) {return 22*8/h;} // approximate width of #
        if(glyph == Glyph.FLAT) {return 18*8/h;} // approximate width of b
        return 16*8/h; // approxiamte width of natural sign
    }
}
