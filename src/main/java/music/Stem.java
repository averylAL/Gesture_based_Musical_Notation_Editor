package music;

import reactions.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Stem extends Duration implements Comparable<Stem> {
    public Staff staff;
    private ArrayList<Head> heads;
    public boolean isUp = true;
    public Beam beam = null;

    public Stem(Staff staff, ArrayList<Head> heads, boolean up) {
        super();
        this.staff = staff;
        isUp = up;
        for (Head h: heads){
            h.unStem();
            h.stem = this;
        }
        this.heads = heads;
        staff.sys.stems.addStem(this);
        setWrongSides();

        addReaction(new Reaction("E-E"){ // ADD FLAG to stem
            public int bid(Gesture g){
                int y = g.vs.yM(), x1 = g.vs.xL(), x2 = g.vs.xH();
                int xS = Stem.this.x();
                if(x1 > xS || x2 < xS){ return UC.noBid; }
                int y1 = Stem.this.yL(), y2 = Stem.this.yH();
                if(y < y1 || y > y2){ return UC.noBid; }
                return Math.abs(y - (y1 + y2)/2) + 60;
            }

            public void act(Gesture g){
                Stem.this.incFlag();
            }
        });

        addReaction(new Reaction("W-W"){ // DEC FLAG to rest
            public int bid(Gesture g){
                int y = g.vs.yM(), x1 = g.vs.xL(), x2 = g.vs.xH();
                int xS = Stem.this.x();
                if(x1 > xS || x2 < xS){ return UC.noBid; }
                int y1 = Stem.this.yL(), y2 = Stem.this.yH();
                if(y < y1 || y > y2){ return UC.noBid; }
                return Math.abs(y - (y1 + y2)/2) + 60;
            }
            public void act(Gesture g){
                Stem.this.decFlag();
            }
        });
    }

    public static Stem getStem(Staff staff, Time time, int y1, int y2, boolean up){
        ArrayList<Head> heads = new ArrayList<>();
        for (Head h: time.heads){
            int yH = h.y();
            if (yH > y1 && yH < y2){heads.add(h);}
        }
        if(heads.size() == 0){return null;} // no stem created if no heads
        Beam b = internalStem(staff.sys, time.x, y1, y2);
        Stem res = new Stem(staff, heads, up);
        if (b != null){b.addStem(res); res.nFlag = 1;}
        return res;
    }

    private static Beam internalStem(Sys sys, int x, int y1, int y2) {
        /*returns non-null IF we find a beam crossed by line*/
        for (Stem s: sys.stems){
            if (s.beam != null && s.x()<x && s.yL()<y2 && s.yH()>y1){
                int bX = s.beam.first().x(), bY = s.beam.first().yBeamEnd();
                int eX = s.beam.last().x(), eY = s.beam.last().yBeamEnd();
                System.out.println("Found Beam" + "" + bX + "" + bY + "" + eX + ""+ eY);
                if (Beam.verticalLineCrossesSegment(x, y1, y2, bX, bY, eX, eY)) {return s.beam;}
            }
        }
        return null;
    }

    public void show(Graphics g) {
        if (nFlag >= -1 && heads.size() > 0) {
            int x = x(), h = staff.H(), yH = yFirstHead(), yB = yBeamEnd();
            g.setColor(Color.BLACK);
            if(nFlag > -2){
                g.drawLine(x, yFirstHead(), x, yB);
            }
            if(nFlag > 0 && beam == null){
                if(nFlag == 1){(isUp ? Glyph.FLAG1D : Glyph.FLAG1U).showAt(g, h, x, yB); }
                if(nFlag == 2){(isUp ? Glyph.FLAG2D : Glyph.FLAG2U).showAt(g, h, x, yB); }
                if(nFlag == 3){(isUp ? Glyph.FLAG3D : Glyph.FLAG3U).showAt(g, h, x, yB); }
                if(nFlag == 4){(isUp ? Glyph.FLAG4D : Glyph.FLAG4U).showAt(g, h, x, yB); }
            }
        }
    }

    public Head firstHead() {
        return heads.get(isUp ? heads.size() - 1 : 0);
    }
    public Head lastHead() {
        return heads.get(isUp ? 0 : heads.size() - 1);
    }
    public int yFirstHead() {
        if(heads.size()==0){return 200;} // gauard empty stems
        Head h = firstHead();
        return h.staff.yLine(h.line);
    }

    public int yBeamEnd() {
        if(heads.size() == 0){return 100;}// gauard empty stems
        if(beam == null || beam.first() == this || beam.last() == this){
            Head h = lastHead();
            int line = h.line;
            line += (isUp ? -7 : 7); // adjust one octave from last head toward the beam
            int flagInc = nFlag > 2 ? 2*(nFlag - 2) : 0; // positive or zero
            line += (isUp ? -flagInc : flagInc); // add 2 lines for every flag over 2
            if((isUp && line > 4) || (!isUp && line < 4)){ line = 4; } // meet center line if we must
            return h.staff.yLine(line);
        }else{
            beam.setMasterBeam();
            return Beam.yOfX(x());
        }
    }

    public int x() {
        if(heads.size()==0) {return 100;} //guard empty stems
        Head h = firstHead();
        return h.time.x + (isUp ? h.W() : 0);
    }
    public int yL(){return isUp? yBeamEnd(): yFirstHead();}
    public int yH(){return isUp? yFirstHead(): yBeamEnd();}

    public String toString(){
        String res = "Stem:" + (isUp ? "^[" : "v[");
        for(Head h : heads) { res += "," + h.line;
        }
        return res+"]";
    }

    public void removeHead(Head h){
        heads.remove(h);
        if(heads.size() == 0){deleteStem();}
        h.stem = null;
        h.wrongSide = false;
    }

    public void deleteStem(){// only call if heads is empty
        if (heads.size() !=0){System.out.println("Deleting stem that had heads on it.");}
        staff.sys.stems.remove(this);
        if (beam != null){beam.removeStem(this);}
        deleteMass();
    }

    public void setWrongSides(){// called by Time.stemHeads
        Collections.sort(heads);
        int i, last, next;
        if (isUp){
            i = heads.size() - 1; last = 0; next = -1;
        }else{
            i = 0; last = heads.size() - 1; next = 1;
        }
        Head ph = heads.get(i);
        ph.wrongSide = false;
        while (i != last){
            i += next;
            Head nh = heads.get(i);
            nh.wrongSide = (ph.staff == nh.staff) && Math.abs(nh.line - ph.line) <= 1 && !ph.wrongSide;
            ph = nh;
        }
    }

    @Override
    public int compareTo(Stem s) {return x() - s.x();}

    //------------------------------------------------Stem.List--------------------------------------------------\\
    public static class List extends ArrayList<Stem> {
        public int yMin = 10000000, yMax = -10000000;

        public void addStem(Stem s) {
            add(s);
            int yF = s.yFirstHead(), yB = s.yBeamEnd();
            if (yF < yMin) {
                yMin = yF;
            }
            if (yF > yMax) {
                yMax = yF;
            }
            if (yB < yMin) {
                yMin = yB;
            }
            if (yB > yMax) {
                yMax = yB;
            }
        }

        public void showMinMax(Graphics g) {
            g.setColor(Color.ORANGE);
            g.drawLine(0, yMin, 100, yMin);
            g.drawLine(0, yMax, 100, yMax);
        }

        public void removeStem(Stem s) {
            remove(s);
        }

        public void sort() {
            Collections.sort(this);
        }

        public boolean fastReject(int y1, int y2) {
            return y1 > yMax || y2 < yMin;
        }

        public ArrayList<Stem> allIntersectors(int x1, int y1, int x2, int y2) {
            ArrayList<Stem> res = new ArrayList<>();
            for (Stem s : this) {
                int x = s.x(), y = Beam.yOfX(x, x1, y1, x2, y2);
                if (x > x1 && x < x2 && y > s.yL() && y < s.yH()) {
                    res.add(s);
                }
            }
                return res;
            }
        }
}
