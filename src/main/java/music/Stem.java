package music;

import reactions.Gesture;
import reactions.Reaction;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Stem extends Duration implements Comparable<Stem>{
    public Staff staff;
    public Head.List heads;
    public boolean isUp = true;
    public Beam beam = null;

    public Stem(Staff staff, Head.List heads, boolean up) {
        this.staff = staff;
        this.isUp = up;

        for(Head h : heads) { h.unStem(); h.stem = this; }
        this.heads = heads;
        staff.sys.stems.addStem(this);
        setWrongSides();

        addReaction(new Reaction("E-E") {//increment flag on stem
            @Override
            public int bid(Gesture g) {
                int y = g.vs.yM(), x1 = g.vs.xL(), x2 = g.vs.xH();
                //int xS = heads.get(0).time.x;
                int xS = Stem.this.X();
                if(x1 > xS || x2 < xS) {return UC.noBid;}
                int y1 = Stem.this.yLo(), y2 = Stem.this.yHi();
                if(y < y1 || y > y2) {return UC.noBid;}
                return Math.abs(y - (y1 + y2)/2) + 60; // biased: Sys E-E reaction can outbid
            }
            @Override
            public void act(Gesture g) {
                Stem.this.incFlag();
            }
        });

        addReaction(new Reaction("W-W") {//decrement flag on stem
            @Override
            public int bid(Gesture g) {
                int y = g.vs.yM(), x1 = g.vs.xL(), x2 = g.vs.xH();
                //int xS = heads.get(0).time.x;
                int xS = Stem.this.X();
                if(x1 > xS || x2 < xS) {return UC.noBid;}
                int y1 = Stem.this.yLo(), y2 = Stem.this.yHi();
                if(y < y1 || y > y2) {return UC.noBid;}
                return Math.abs(y - (y1 + y2)/2);
            }
            @Override
            public void act(Gesture g) {
                Stem.this.decFlag();
                if(nFlag == 0 && beam != null){beam.deleteBeam();}
            }
        });
    }

    /**
     * Factoring method
     * gets a stem IF there are heads that match the y values at the given time
     * comment out the stemHeads class in Time; comment out the routine joinStem in Head
     */
    public static Stem getStem(Staff staff, Time time, int y1, int y2, boolean up){
        Head.List heads = new Head.List();
        for(Head h : time.heads){
            int yh = h.Y();
            if(yh > y1 && yh < y2){ heads.add(h);}
        }
        if(heads.size() == 0){return null;} // no stem created if no heads
        Beam b = internalStem(staff.sys, time.x, y1, y2); //this could be internal stem
        Stem res = new Stem(staff, heads, up); // create the stem
        if(b!=null){ // if it was internal, then join the beam.
            b.addStem(res);
            res.nFlag = 1;
        }
        return res;
    }

    public static Beam internalStem(Sys sys, int x, int y1, int y2){ // returns non-null IF we find a beam crossed by line
        for(Stem s : sys.stems){
            if(s.beam !=null && s.X()<x && s.yLo()<y2 && s.yHi()>y1){
                int bx = s.beam.first().X(), by = s.beam.first().yBeamEnd();
                int ex = s.beam.last().X(), ey = s.beam.last().yBeamEnd();
                if(Beam.verticalLineCrossesSegment(x,y1,y2,bx,by,ex,ey)){return s.beam;}
            }
        }
        return null;
    }

    public void show(Graphics g) {
        if(nFlag >= -1 && heads.size() > 0) {
            int x = X(), h = staff.fmt.H, yH = yFirstHead(), yB = yBeamEnd();
            g.drawLine(x,yH,x,yB);
            if(nFlag > 0 && beam == null){
                if(nFlag == 1){(isUp ? Glyph.FLAG1D : Glyph.FLAG1U).showAt(g, h, x, yB);}
                if(nFlag == 2){(isUp ? Glyph.FLAG2D : Glyph.FLAG2U).showAt(g, h, x, yB);}
                if(nFlag == 3){(isUp ? Glyph.FLAG3D : Glyph.FLAG3U).showAt(g, h, x, yB);}
                if(nFlag == 4){(isUp ? Glyph.FLAG4D : Glyph.FLAG4U).showAt(g, h, x, yB);}
            }
        }
    }

    public Head firstHead() {return heads.get(isUp ? heads.size() - 1 : 0);}
    public Head lastHead(){return  heads.get(isUp ? 0 : heads.size() - 1);}
    public int yFirstHead(){
        if(heads.size() == 0) {return 200;}
        Head h = firstHead();
        return h.staff.yOfLine(h.line);
    }
    public int X(){
        if(heads.size() == 0) {return 100;}
        Head h = firstHead();
        return h.time.x + (isUp ? h.W() : 0);
    }
    public int yBeamEnd() {
        if(heads.size() == 0) {return 100;}
        if(isInternalStem()){beam.setMasterBeam(); return Beam.yOfX(X());}
        Head h = lastHead();
        int line = h.line;
        line += (isUp? -7 : 7); // default length is one octave from last head on the beam
        int flagInc = nFlag > 2 ? 2*(nFlag - 2) : 0; // if more than 2 flags we adjust stem end
        line += (isUp? -flagInc : flagInc); //direction of adjustment depends on up or down stem
        if((isUp && line > 4) || (!isUp && line < 4)){ line = 4; } // meet center line if we must
        return h.staff.yOfLine(line);
    }

    public boolean isInternalStem() {
        if(beam == null){return false;}
        if(this == beam.first() || this == beam.last()){return false;}
        return true;
    }

    public int yLo(){return isUp ? yBeamEnd() : yFirstHead();}
    public int yHi(){return isUp ? yFirstHead() : yBeamEnd();}

    public void deleteStem() {
        if(heads.size() != 0) {System.out.println("Deleting stem with heads.");}
        staff.sys.stems.remove(this);
        if(beam != null){beam.removeStem(this);}
        deleteMass();
    }

    public void setWrongSides() {// called by Time.stemHeads
        Collections.sort(heads);
        int i, last, next;
        if(isUp){i = heads.size() - 1; last = 0; next = -1;} else{i = 0; last = heads.size() - 1; next = 1;}
        Head ph = heads.get(i); ph.wrongSide = false; // first head is always right;ph = Previous Head
        while(i != last){
            i += next;
            Head nh = heads.get(i);//nh = Next Head
            //nh.wrongSide = (Math.abs(nh.line - ph.line) <=1 && !ph.wrongSide);
            //this line does not do cross staff seconds
            nh.wrongSide = ((ph.staff == nh.staff) && (Math.abs(nh.line - ph.line) <= 1) && !ph.wrongSide);
            ph = nh;
        }
    }

    @Override
    public int compareTo(Stem s) {return X() - s.X();}

    //----------------------------------Stem.List-----------------------------
    public static class List extends ArrayList<Stem> {
        public int yMin = 1_000_000, yMax = -1_000_000;
        public void addStem(Stem s){
            add(s);
            if(s.yLo() < yMin){yMin = s.yLo();}
            if(s.yHi() > yMax){yMax = s.yHi();}
        }
        public boolean fastReject(int y){return y > yMax || y < yMin;}
        public void sort(){Collections.sort(this);}
        public ArrayList<Stem> allIntersectors(int x1, int y1, int x2, int y2){
            ArrayList<Stem> res = new ArrayList<>();
            for(Stem s : this) {
                if(Beam.verticalLineCrossesSegment(s.X(), s.yLo(), s.yHi(), x1, y1, x2, y2)){
                    res.add(s);
                }
            }
            return res;
        }
    }
}
