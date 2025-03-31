package music;

import reactions.*;

import java.awt.*;
import java.util.ArrayList;

public class Head extends Mass implements Comparable<Head>{
    public Staff staff;
    public int line;//line is y coordinate in disguise, 0 equals top line of staff
    public Time time;
    public Glyph forcedGlyph = null; //null means use the normal Glyph calculated from the flag count
    public Stem stem = null; // heads are created with no stem so the user can choose direction
    public boolean wrongSide = false;
    public Glyph accidental = null; //default set to null, do not show key signatures first until we draw NE-SE, SE-NE

    public Head(Staff staff, int x, int y){
        super("NOTE");
        this.staff = staff;
        time = staff.sys.getTime(x);

        int H = staff.fmt.H; //size of head
        int top = staff.yTop() - H; // one space above top line
        //line = (y - top + H/2)/H - 1;//snap to nearest line
        line = staff.lineOfY(y);
        //System.out.println("Line: " + line);//debug
        time.heads.add(this);

        addReaction(new Reaction("S-S"){//Stem or unStem heads
            @Override
            public int bid(Gesture g){
                int x = g.vs.xM(), y1 = g.vs.yL(), y2 = g.vs.yH();
                int W = Head.this.W(), Y = Head.this.Y();
                System.out.println("Head reaction");//debug
                if(y1 > y || y2 < y){ return UC.noBid; } // heads not in y range reject this gesture
                int hl = Head.this.time.x, hr = hl + W; // left and right side of Head.
                if(x < hl - W || x > hr + W){ return UC.noBid; } // must be reasonably close to the head.
                System.out.println("Head reaction x, y ok");//debug
                if(x < (hl + W/2)){ return hl - x; }
                if(x > (hr - W/2)){ return x - hr; }
                return UC.noBid;
            }
            @Override
            public void act(Gesture g){
                int x = g.vs.xM(), y1 = g.vs.yL(), y2 = g.vs.yH(); // gesture locations
                Staff staff = Head.this.staff; // Head parameters
                Time t = Head.this.time;
                int W = Head.this.W();
                boolean up = x > (t.x + W/2); //where the gesture is related to actual Notes head
                if(Head.this.stem == null){ // winner of bid gets to choose between stem or unStem action
                    //t.stemHeads(staff, up, y1,y2); // staff and up needed to create the stem
                    Stem.getStem(staff, time, y1, y2, up);
                }else{
                    t.unStemHeads(y1,y2);
                }
            }
        });

        addReaction(new Reaction("DOT"){
            @Override
            public int bid(Gesture g){
                int xh = X(), yh = Y(), h = staff.fmt.H, w = W();
                int x = g.vs.xM(), y = g.vs.yM();
                if(x < xh || x > xh + 2*w || y < yh - h || y > yh + h){return UC.noBid; }
                return Math.abs(xh + w - x) + Math.abs(yh - y);
            }
            @Override
            public void act(Gesture g){
                if(Head.this.stem != null){Head.this.stem.cycleDot();}
            }
        });

        addReaction(new Reaction("NE-SE"){//increment the accidental sharp
            @Override
            public int bid(Gesture g){
                int x = g.vs.xM(), y = g.vs.yL();
                int hC = X() + W()/2, hY = Y();//hC is head center
                if (x < hC - 40 || x > hC + 40){ return UC.noBid; }
                if (y < hY - 40 || y > hY + 40){ return UC.noBid; }
                return Math.abs(hC - x) + Math.abs(hY - y);
            }
            @Override
            public void act(Gesture g){
                incAccidental();
            }
        });

        addReaction(new Reaction("SE-NE"){//decrement the accidental sharp
            @Override
            public int bid(Gesture g){
                int x = g.vs.xM(), y = g.vs.yH();
                int hC = X() + W()/2, hY = Y();//hC is head center
                if (x < hC - 40 || x > hC + 40){ return UC.noBid; }
                if (y < hY - 40 || y > hY + 40){ return UC.noBid; }
                return Math.abs(hC - x) + Math.abs(hY - y);
            }
            @Override
            public void act(Gesture g){
                decAccidental();
            }
        });
    }

    private void incAccidental() {
        if (accidental == null){ accidental = Glyph.SHARP; return;}
        if (accidental == Glyph.SHARP){ accidental = Glyph.DSHARP; return;}
        if (accidental == Glyph.DSHARP){ accidental = null; return;}
        if (accidental == Glyph.FLAT){ accidental = Glyph.NATURAL; return;}
        if (accidental == Glyph.DFLAT){ accidental = Glyph.FLAT; return;}
        if (accidental == Glyph.NATURAL){ accidental = Glyph.SHARP; return;}
    }

    private void decAccidental() {
        if (accidental == null){ accidental = Glyph.FLAT; return;}
        if (accidental == Glyph.SHARP){ accidental = Glyph.NATURAL; return;}
        if (accidental == Glyph.DSHARP){ accidental = Glyph.SHARP; return;}
        if (accidental == Glyph.FLAT){ accidental = Glyph.DFLAT; return;}
        if (accidental == Glyph.DFLAT){ accidental = null; return;}
        if (accidental == Glyph.NATURAL){ accidental = Glyph.FLAT; return;}
    }

    public void show(Graphics g){
        int H = staff.fmt.H;
        //Glyph.HEAD_Q.showAt(g, H, time.x, staff.yTop() + line*H);
//        g.setColor(wrongSide ? Color.GREEN : Color.BLUE);
//        if(stem != null && stem.heads.size() != 0 && this == stem.firstHead()) {
//            g.setColor(Color.RED);//first head
//        }
        g.setColor(stem == null ? Color.RED : Color.BLACK);
        (forcedGlyph != null ? forcedGlyph : normalGlyph()).showAt(g, H, X(), Y());
        if(stem != null){
            int off = UC.AugDotOffSet, sp = UC.AugDotSpacing;
            for(int i = 0; i < stem.nDot; i++){
                g.fillOval(time.x + off + i*sp, Y() - 3*H/2, 2*H/3, 2*H/3);
            }
        }
        if (accidental != null){accidental.showAt(g, H, X() - UC.accidentalHeadGap, Y());}
    }

    public int X() {
        int res = time.x;
        if(wrongSide){res += (stem!= null && stem.isUp) ? W() : -W();}
        return res;
    }
    public int Y(){return staff.yOfLine(line);}
    public Glyph normalGlyph(){
        if(stem == null){return Glyph.HEAD_Q;}
        if(stem.nFlag == -1){return Glyph.HEAD_HALF;}
        if(stem.nFlag == -2){return Glyph.HEAD_W;}
        return Glyph.HEAD_Q;
    }

    public int W(){
        return 24*staff.fmt.H/10;
    }

    public void delete(){//STUB
        time.heads.remove(this);
    }

    public void unStem() {
        if(stem != null){  // if stem is null, this head is already unStemmed
            stem.heads.remove(this); // remove old stem
            if(stem.heads.size() == 0){stem.deleteStem();} // delete old stem if it becomes empty
            stem = null;
            wrongSide = false;
        }
    }

    /*
    public void joinStem(Stem s) {
        if(stem != null){unStem();}// make sure that this head is NOT on some other stem
        s.heads.add(this);
        stem = s;
    }
     */

    public int compareTo(Head h){// this is the code required for Comparable that makes the sort work.
        return (staff.iStaff != h.staff.iStaff) ? staff.iStaff - h.staff.iStaff : line - h.line;
    }

    //----------------Head.List---------------------
    public static class List extends ArrayList<Head> {}
}
