package music;

import graphics.*;
import reactions.*;
import java.awt.*;

public class Bar extends Mass{
    private static final int FAT = 2, RIGHT = 4, LEFT = 8; // bits in barType

    /* 0=single; 1=double; 2=fine; if either of bits RIGHT or LEFT are set it is a repeat
      i.e. repeats dominate any of the lower bits.
      Intended reactions - S-S on empty space creates Bar, S-S on existing bar cycles between types.
      DOT to the right or left of Bar, toggles Dots (repeats) on that particular side.
    */

    public Sys sys;
    public int x, barType = 0;
    public Key key = null;

    public Bar(Sys sys, int x) {
        super("BACK");
        this.sys = sys; barType = 0;
        this.x = x;
        int right = sys.page.right();
        if(Math.abs(right - x) < UC.barToMarginSnap) {this.x = right;}
        barType = 0;

        addReaction(new Reaction("S-S"){ // cycle this Bar
            public int bid(Gesture g){
                int x = g.vs.xM();
                if(Math.abs(x - Bar.this.x) > UC.barToMarginSnap){return UC.noBid;}
                int y1 = g.vs.yL(), y2 = g.vs.yH();
                if(y1 < Bar.this.sys.yTop()-20 || y2 > Bar.this.sys.yBot()+20) {
                    return UC.noBid;
                } // y1 && y1 both in sys range
                return Math.abs(x - Bar.this.x);
            }
            public void act(Gesture g){
                Bar.this.cycleType();
            }
        });

        addReaction(new Reaction("DOT"){ // Dot this Bar
            public int bid(Gesture g){
                int x = g.vs.xM();
                int y = g.vs.yM();
                if(y < Bar.this.sys.yTop() || y > Bar.this.sys.yBot()){ return UC.noBid; }
                int dist = Math.abs(x - Bar.this.x);
                if (dist > 3 * ((Page) sys.page).maxH) { return UC.noBid; }
                return dist;
            }
            public void act(Gesture g){
                if(g.vs.xM() < Bar.this.x) {
                    Bar.this.toggleLeft();
                } else{
                    Bar.this.toggleRight();
                }
            }
        });

        addReaction(new Reaction("E-E"){ // increment key on double bar
            public int bid(Gesture g){
                if(barType != 1){return UC.noBid;} // you can only increment Key on double bar
                int x1 = g.vs.xL(), x2 = g.vs.xH(); // Bar.this.x == x
                if(x1 > x || x2 < x){return UC.noBid;}
                int y = g.vs.yM();
                if(y < sys.yTop() || y > sys.yBot()){return UC.noBid;}
                // here if gesture crossed a barline inside the system bounds
                return Math.abs(x - (x1+x2)/2); // how far is gesture midpoint from x
            }

            public void act(Gesture g){
                Bar.this.incKey();
            }
        });

        addReaction(new Reaction("W-W"){
            public int bid(Gesture g){
                if(barType != 1){return UC.noBid;} // you can only decrement Key on double bar
                int x1 = g.vs.xL(), x2 = g.vs.xH(); // Bar.this.x == x
                if(x1 > x || x2 < x){return UC.noBid;}
                int y = g.vs.yM();
                if(y < sys.yTop() || y > sys.yBot()){return UC.noBid;}
                // here if gesture crossed a barline inside the system bounds
                return Math.abs(x - (x1+x2)/2); // how far is gesture midpoint from x
            }

            public void act(Gesture g){
                Bar.this.decKey();
            }
        });
    }

    public void cycleType(){barType++; if(barType > 2){barType = 0;}}
    public void toggleLeft(){barType = barType^LEFT;}
    public void toggleRight(){barType = barType^RIGHT;}

    public void show(Graphics g){
        g.setColor(Color.BLACK);
        Sys.Fmt SYSFMT = APP.get.sysfmt(sys.page);
        int yTop = sys.yTop(), H = SYSFMT.maxH, y1 = 0, bot; // y1,bot mark top and bot of connected component
        boolean justSawBreak = true; // signals when we are at the top of a new connected component
        //for(Staff.Fmt sf : sys.page.sysfmt) {
        for(Staff.Fmt sf : SYSFMT) {
            int top = yTop + sf.dy; // top of this staff
            bot = top + sf.height();  // bottom of this staff
            if(justSawBreak){
                y1 = top;
            } // remember start of connected component
            justSawBreak = !sf.barContinues;
            if(justSawBreak){ // we now have a connected component from y1 to y2
                if(y1 == bot){
                    y1 -= 2*H;
                    bot += 2*H;
                } // this is a fix for isolated drum, single line staffs.

                drawLines(g, x, y1, bot);  // lines show only at end of connected components
            }
            if(barType > 3){
                drawDots(g, x, top);
            }
        }
        if(barType==1 && key != null){key.drawOnSys(g,sys,x+UC.barKeyOffset);}
    }

    // graphics helpers to draw different bar types
    public static void wings(Graphics g, int x, int y1, int y2, int dx, int dy){
        g.drawLine(x, y1, x+dx, y1-dy);
        g.drawLine(x, y2, x+dx, y2+dy);
    }
    public static void fatBar(Graphics g, int x, int y1, int y2, int dx){g.fillRect(x, y1, dx, y2-y1);}
    public static void thinBar(Graphics g, int x, int y1, int y2){g.drawLine(x, y1, x, y2);}
    public void drawDots(Graphics g, int x, int top){
        // from top of single staff
        // notice - this code ASSUMES nLine is 5. We will need to fix if we ever allow
        // not-standard staffs.
        int H = ((Page) sys.page).maxH;
        if((barType & LEFT) != 0){
            g.fillOval(x-3*H, top+11*H/4, H/2, H/2);
            g.fillOval(x-3*H, top+19*H/4, H/2, H/2);
        }
        if((barType & RIGHT) != 0){
            g.fillOval(x+3*H/2, top+11*H/4, H/2, H/2);
            g.fillOval(x+3*H/2, top+19*H/4, H/2, H/2);
        }
    }
    public void drawLines(Graphics g, int x, int y1, int y2){
        int H = APP.get.sysfmt(sys.page).maxH;
        if(barType == 0){ thinBar(g, x, y1, y2); }
        if(barType == 1){ thinBar(g, x, y1, y2);thinBar(g, x - H, y1, y2); }
        if(barType == 2){ fatBar(g, x - H, y1, y2, H);thinBar(g, x - 2*H, y1, y2); }
        if(barType >= 4){
            fatBar(g, x - H, y1, y2, H); // all repeats have fat bar
            if((barType & LEFT) != 0){ thinBar(g, x - 2*H, y1, y2);wings(g, x - 2*H, y1, y2, -H, H); }
            if((barType & RIGHT) != 0){ thinBar(g, x + H, y1, y2);wings(g, x + H, y1, y2, H, H); } }
    }

    public void incKey(){
        if(key == null){key = new Key();}
        if(key.glyph == Glyph.NATURAL){key.glyph = Glyph.SHARP; key.n=1; return;}
        if(key.glyph == Glyph.FLAT){key.glyph = Glyph.NATURAL; return;}
        // else was sharp key
        if(key.n<7){key.n++;}
    }

    public void decKey(){
        if(key == null){key = new Key();}
        if(key.glyph == Glyph.NATURAL){key.glyph = Glyph.FLAT; key.n=-1; return;}
        if(key.glyph == Glyph.SHARP){key.glyph = Glyph.NATURAL; return;}
        // else was flat key
        if(key.n>-7){key.n--;}
    }
}
