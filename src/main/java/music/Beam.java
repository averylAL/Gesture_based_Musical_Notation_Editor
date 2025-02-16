package music;

import graphics.*;
import reactions.*;

import java.awt.*;

public class Beam extends Mass{
    public Stem.List stems = new Stem.List();

    public Beam(Stem a, Stem b){
        super("NOTE");
        stems.add(a); stems.add(b);
        a.beam = this; b.beam = this;
        a.nFlag = 1; b.nFlag = 1;
        stems.sort();
    }

    public String toString(){return " Beam ";}

    public Stem first(){return stems.get(0);}
    public Stem last(){return stems.get(stems.size()-1);}

    public static int mX1, mY1, mX2, mY2;//the coordinates for master beam

    public static int yOFX(int x, int x1, int y1, int x2, int y2){
        int dY = y2 - y1, dX = x2 - x1;
        return (x - x1) * dY / dX + y1;
    }

    public static int yOFX(int x){
        int dY = mY2 - mY1, dX = mX2 - mX1;
        return (x - mX1) * dY / dX + mY1;
    }

    public void setMasterBeam(){
        mX1 = first().x();
        mY1 = first().yBeamEnd();
        mX2 = last().x();
        mY2 = last().yBeamEnd();
    }

    static {
        int[] foo = {0, 0, 0, 0};
        poly = new Polygon(foo, foo, 4);// construct poly as 4 point buffer
    }

    public void deleteBeam(){
        for(Stem s : stems){s.beam = null;} // remove stem associations with beam.
        deleteMass(); // remove beam from layers
    }

    public void addStem(Stem s){
        if(s.beam == null){
            stems.add(s); // stem joins list in beam
            s.beam = this; // stem now refers to this beam
            s.nFlag = 1; // if beamed stem has at least one flag
            stems.sort(); // stems should be sorted by increasing x value
        }
    }

    public void removeStem(Stem s){
        if(s == first() || s == last()){deleteBeam();} else {stems.remove(s); stems.sort();}
    }
    public void show(Graphics g){ g.setColor(Color.BLACK); drawBeamGroup(g); }

    private void drawBeamGroup(Graphics g){
        setMasterBeam(); // defines master beam coords: mx1, my1, mx2, my2
        Stem firstStem = first();
        int H = firstStem.staff.H(); int sH = firstStem.isUp ? H : -H; // signed H needed for beamStack
        int nPrev = 0, nCur = firstStem.nFlag, nNext = stems.get(1).nFlag; // flag count for 3 stems
        int px; int cx = firstStem.x(); // x location of previous stem and current stem
        int bx = cx + 3*H; // forward leanding beamlet on first stem runs from cx to bx
        if(nCur > nNext){drawBeamStack(g, nNext, nCur, cx, bx, sH);} // beamlets on first stem point right
        for(int cur = 1; cur <stems.size(); cur++){
            Stem sCur = stems.get(cur); px = cx; cx = sCur.x();
            nPrev = nCur; nCur = nNext; nNext = (cur <(stems.size()-1))? stems.get(cur +1).nFlag : 0;
            int nBack = Math.min(nPrev, nCur);
            drawBeamStack(g, 0, nBack, px, cx, sH); // draw beams back to previous stem.
            if(nCur > nPrev && nCur > nNext){ // we have beamlets on this stem.
                if(nPrev < nNext){  // beamlets lean toward side with more beams.
                    bx = cx +3*H;
                    drawBeamStack(g, nNext, nCur, cx, bx, sH);
                }else{
                    bx = cx -3*H;
                    drawBeamStack(g, nPrev, nCur, bx, cx, sH);
                }
            }
        }
    }
    // ----------static variables and function ---------
    public static int mx1,my1,mx2,my2; // coordinates of THE master beam
    public static Polygon poly; // buffer to hold the polygon that makes up a single beam
    static {int[] foo = {0,0,0,0}; poly = new Polygon(foo,foo,4);}


    // math function to find y on a sloped line segment one is buffered one is just arguments.
    public static void setMasterBeam(int x1, int y1, int x2, int y2){ // use static master beam buffer for yOfx calculations
        mx1 = x1; my1 = y1; mx2 = x2; my2 = y2;
    }
    public static int yOfX(int x){int dy = my2-my1, dx = mx2-mx1; return ((x-mx1)*dy)/dx + my1;}
    public static int yOfX(int x, int x1, int y1, int x2, int y2){
        int dy = y2-y1, dx = x2-x1;
        return ((x-x1)*dy)/dx + y1;
    }

    public static boolean verticalLineCrossesSegment(int x, int y1, int y2, int bx, int by, int ex, int ey){
        if(x < bx || x > ex){return false;}
        int y = yOfX(x, bx, by, ex, ey);
        if(y1<y2){return (y1 < y && y<y2);} else {return (y2<y && y<y1);}
    }

    // draws from n1 to n2 using  segment x1,y1, x2,y2
    public static void drawBeamStack(Graphics g, int n1, int n2, int x1, int x2, int h){
        int y1 = yOfX(x1), y2 = yOfX(x2);
        for(int i = n1; i<n2; i++){ // draw the full beams
            setPoly(x1, y1+i*2*h, x2, y2+i*2*h, h);
            g.fillPolygon(poly);
        }
    }

    public static void setPoly(int x1, int y1, int x2, int y2, int h){
        int[] a = poly.xpoints; a[0] = x1; a[1] = x2; a[2] = x2; a[3] = x1;
        a = poly.ypoints; a[0] = y1; a[1] = y2; a[2] = y2 + h; a[3] = y1 + h;
    }
}
