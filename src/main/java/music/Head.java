package music;

import reactions.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

public class Head extends Mass implements Comparable<Head> {
    public static Comparator<Head> orderByY = new Comparator<Head>(){
        public int compare(Head h1, Head h2){
            return (h1.staff.ndx != h2.staff.ndx) ? h1.staff.ndx - h2.staff.ndx : h1.line - h2.line;
        }
    };

    public int x, line; // line is a yCoord in disguise. Line 0 is top line, line 1 is first space, line 2 is next line down.
    public Time time;
    public Staff staff;
    public Glyph forcedGlyph = null;
    public Stem stem = null; // heads are created with no stem so the user can choose direction.
    public boolean wrongSide = false;
    public String toString(){return " Head:"+line;}
    //public Accidental accidental = null;

    public Head(Staff staff, int x, int y) {
        super("NOTE");
        this.staff = staff;
        this.x = x;
        this.time = staff.sys.getTime(x);
        time.heads.add(this);
        int H = staff.fmt.H;
        int top = staff.yTop() - H; // y could be in the space above the top line which becomes -1
        this.line = staff.lineOfY(y); // snap y to nearest line
        System.out.println("Line: "+line); // debug code, watch it then comment it out.

        addReaction(new Reaction("S-S"){ // Stem or unStem heads
            public int bid(Gesture g){
                int x = g.vs.xM(), y1 = g.vs.yL(), y2 = g.vs.yH();
                int W = Head.this.W(), hy = Head.this.y();
                if(y1 > y || y2 < y){return UC.noBid;} // heads not in y range reject this gesture
                int hl = Head.this.time.x, hr = hl + W; // left and right side of Head.
                if(x < hl-W || x > hr+W){return UC.noBid;} // must be reasonably close to the head.
                if(x < (hl+W/2)){return hl-x;}
                if(x > (hr-W/2)){return x-hr;}
                return UC.noBid;
            }
            public void act(Gesture g){
                int x = g.vs.xM(), y1 = g.vs.yL(), y2 = g.vs.yH(); // gesture locations
                Staff staff = Head.this.staff; // Head parameters
                Time t = Head.this.time;
                int W = Head.this.W();
                boolean up = x > (t.x+ W/2); //
                if(Head.this.stem == null){ // winner of bid gets to choose between stem or unStem action
                    Stem.getStem(staff, t, y1, y2, up);
                }else{
                    t.unStemHeads(y1,y2);
                }
            }
        });

        addReaction(new Reaction("DOT") {//add augmentation dots
            public int bid(Gesture g) {
                int xH = Head.this.x(), yH = Head.this.y(), H = Head.this.staff.H(), W = Head.this.W();
                int x = g.vs.xM(), y = g.vs.yM();
                if (x < xH || x > xH + 2 * W || y < yH - H || y > yH + H) {
                    return UC.noBid;
                }
                return Math.abs(xH + W - x) + Math.abs(y - yH);
            }
            public void act(Gesture g) {
                if (Head.this.stem != null) {
                    Head.this.stem.cycleDot();
                }
            }
        });
    }

    public void show(Graphics g){
        int H = staff.H();
        g.setColor(stem == null ? Color.RED : Color.BLACK);
        //if(wrongSide){g.setColor(Color.CYAN);}
        Glyph glyph = Glyph.HEAD_Q;
        (forcedGlyph != null ? forcedGlyph : normalGlyph()).showAt(g, H, x(), y());
        if (stem != null){
            int off = UC.restFirstDotOffset, sp = UC.dotSpacingRest;
            for (int i = 0; i < stem.nDot; i++){
                g.fillOval(time.x + off + i * sp, y() - 3 * H / 2, H * 2 / 3, H * 2 / 3);
            }
        }
    }

    public Glyph normalGlyph(){
        if(stem == null){ return Glyph.HEAD_Q; }
        if(stem.nFlag == -1){ return Glyph.HEAD_HALF; }
        if(stem.nFlag == -2){ return Glyph.HEAD_W; }
        return Glyph.HEAD_Q;
    }

    public int W() {return 24 * staff.H() / 10;} //Width of a note head, RIGHT = LEFT + W()
    public int y() {return staff.lineOfY(line);}
    public int x() {
        int res = time.x;
        if(wrongSide){res += (stem!= null && stem.isUp) ? W() : -W();}
        return res;
    }

    public void unStem(){
        if (stem != null){
            stem.removeHead(this);
        }
    }

    public int compareTo(Head h){ // this is the code required for Comparable that makes the sort work.
        return (staff.ndx != h.staff.ndx) ? staff.ndx - h.staff.ndx : line - h.line;
    }

    //-------------------HEAD LIST ---------------------------------------------------
    public static class List extends ArrayList<Head> {}
}
