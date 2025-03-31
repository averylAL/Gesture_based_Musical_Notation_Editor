package music;

import reactions.*;

import java.awt.*;

public class Beam extends Mass {
    public Stem.List stems = new Stem.List();

    public Beam(Stem f, Stem l) {//first, last
        super("NOTE");
        addStem(f);
        addStem(l);
    }

    public Stem first(){return stems.get(0);}
    public Stem last(){return stems.get(stems.size()-1);}

    public void deleteBeam(){//note: stems still exist, flags and dots still exist, just remove beam from stems
        for(Stem s : stems){s.beam = null;}
        deleteMass();
    }

    public void addStem(Stem s){
        if(s.beam == null){
            stems.add(s);
            s.beam = this;
            s.nFlag = 1;
            stems.sort();
        }
    }

    public static int yOfX(int x, int x1, int y1, int x2, int y2){//linear functions
        int dy = y2 -y1, dx = x2 - x1;
        return (x - x1) * dy/dx + y1;
    }

    public static boolean verticalLineCrossesSegment(int x, int y1, int y2, int bx, int by, int ex, int ey){
        if(x < bx || x > ex){return false;} //X outside the range of section
        int y = yOfX(x, bx, by, ex, ey);
        if(y1<y2){
            return (y1 < y && y<y2);
        } else {return (y2<y && y<y1);}
    }

    public static int mx1, my1, mx2, my2;//coordinates for the master Beam
    public static int yOfX(int x){
        int dy = my2 - my1, dx = mx2 - mx1;
        return (x - mx1)*dy/dx + my1;
    }
    public static void setMasterBeam(int x1, int y1, int x2, int y2){
        mx1 = x1; my1 = y1; mx2 = x2; my2 = y2;
    }
    public void setMasterBeam(){
        mx1 = first().X(); my1 = first().yBeamEnd(); mx2 = last().X(); my2 = last().yBeamEnd();
    }

    public static Polygon poly;
    static {int[] foo={0,0,0,0}; poly = new Polygon(foo, foo, 4);}
    public static void setPoly(int x1, int y1, int x2, int y2, int h){
        int[] a = poly.xpoints;
        a[0] = x1; a[1] = x2; a[2] = x2; a[3] = x1;
        a = poly.ypoints;
        a[0] = y1; a[1] = y2; a[2] = y2 + h; a[3] = y1 + h;
    }

    public static void drawBeamStack(Graphics g, int n1, int n2, int x1, int x2, int h){
        int y1 = yOfX(x1), y2 = yOfX(x2);
        for(int i = n1; i < n2; i++){
            setPoly(x1, y1 + i*2*h, x2, y2 + i*2*h, h);
            g.fillPolygon(poly);
        }
    }

    public void show(Graphics g){ g.setColor(Color.BLACK); drawBeamGroup(g); }

    private void drawBeamGroup(Graphics g){
        setMasterBeam(); // defines master beam coords: mx1, my1, mx2, my2
        Stem firstStem = first();
        int H = firstStem.staff.fmt.H;
        int sH = firstStem.isUp ? H : -H; //signed H value for beamStack
        int nPrev = 0, nCur = firstStem.nFlag, nNext = stems.get(1).nFlag; // flag count for 3 stems
        int px; int cx = firstStem.X(); // x location of previous stem and current stem
        int bx = cx + 3*H; // forward leaning beamlet on first stem runs from cx to bx
        if(nCur > nNext){drawBeamStack(g, nNext, nCur, cx, bx, sH);} // beamlets on first stem point right
        for(int cur = 1; cur <stems.size(); cur++){
            Stem sCur = stems.get(cur);
            px = cx;
            cx = sCur.X();
            nPrev = nCur;
            nCur = nNext;
            nNext = (cur <(stems.size() - 1)) ? stems.get(cur + 1).nFlag : 0;
            int nBack = Math.min(nPrev, nCur);
            drawBeamStack(g, 0, nBack, px, cx, sH); // draw beams back to previous stem.
            if(nCur > nPrev && nCur > nNext){ // have beamlets on this stem.
                if(nPrev < nNext){  // beamlets lean toward side with more beams.
                    bx = cx + 3*H;
                    drawBeamStack(g, nNext, nCur, cx, bx, sH);
                }else{
                    bx = cx - 3*H;
                    drawBeamStack(g, nPrev, nCur, bx, cx, sH);
                }
            }
        }
    }

    public void removeStem(Stem s) {
        if (s == first() || s == last()){
            deleteBeam();
        } else {stems.remove(s); stems.sort();}
    }
}
