package music;

import reactions.*;

import java.awt.*;

public class Bar extends Mass {
    private static final int FAT = 2, RIGHT = 4, LEFT = 8;

    /* 0=single; 1=double; 2=fine; if either of bits RIGHT or LEFT are set it is a repeat
      i.e. repeats dominate any of the lower bits.
      Intended reactions - S-S on empty space creates Bar, S-S on existing bar cycles between types.
      DOT to the right or left of Bar, toggles Dots (repeats) on that particular side.
    */

    public Sys sys;
    public int x, barType = 0;//0 is default thin line; 1, for double line
    public Key key = null;//null bkz most bar do not defined key

    public Bar(Sys sys, int x) {
        super("BACK");
        this.sys = sys;
        this.x = x;
        int right = sys.page.margins.right;
        if (Math.abs(right - this.x) < UC.barToMarginSnap){this.x = right;}

        addReaction(new Reaction("S-S") {//Cycle this bar
            @Override
            public int bid(Gesture g){
                int x = g.vs.xM();
                if (Math.abs(x - Bar.this.x) > UC.barToMarginSnap) {return UC.noBid;}
                int y1 = g.vs.yL(), y2 = g.vs.yH();
                if(y1 < Bar.this.sys.yTop()-20 || y2 > Bar.this.sys.yBot()+20){return UC.noBid;} // y1 && y1 both in sys range, anything within this range can draw bar line
                return Math.abs(x - Bar.this.x);// biggest bid: barToMarginSnap
            }
            @Override
            public void act(Gesture g){
                Bar.this.cycleType();
            }
        });

        addReaction(new Reaction("DOT"){ //Dot this Bar
            @Override
            public int bid(Gesture g){
                int x = g.vs.xM(), y = g.vs.yM();
                if(y < Bar.this.sys.yTop() || y > Bar.this.sys.yBot()){ return UC.noBid; }
                int dist = Math.abs(x - Bar.this.x); //how far we do DOT from bar line
                if(dist > 3*sys.page.maxH){ return UC.noBid; }
                return dist;
            }
            @Override
            public void act(Gesture g){
                if(g.vs.xM() < Bar.this.x){
                    Bar.this.toggleLeft();
                }else { Bar.this.toggleRight(); }
            }
        });

        addReaction(new Reaction("E-E"){ //increment key on double bar
            @Override
            public int bid(Gesture g){
                if(barType != 1){return UC.noBid;} //can only increment Key on double bar
                int x1 = g.vs.xL(), x2 = g.vs.xH(); // Bar.this.x == x
                if(x1 > x || x2 < x){return UC.noBid;}
                int y = g.vs.yM();
                if(y < sys.yTop() || y > sys.yBot()){return UC.noBid;}
                // here if gesture crossed a barline inside the system bounds
                return Math.abs(x - (x1 + x2)/2); // how far is gesture midpoint from x
            }
            @Override
            public void act(Gesture g){
               Bar.this.incKey();
            }
        });

        addReaction(new Reaction("W-W"){ //decrement key on double bar
            @Override
            public int bid(Gesture g){
                if(barType != 1){return UC.noBid;} //can only increment Key on double bar
                int x1 = g.vs.xL(), x2 = g.vs.xH(); // Bar.this.x == x
                if(x1 > x || x2 < x){return UC.noBid;}
                int y = g.vs.yM();
                if(y < sys.yTop() || y > sys.yBot()){return UC.noBid;}
                // here if gesture crossed a barline inside the system bounds
                return Math.abs(x - (x1 + x2)/2); // how far is gesture midpoint from x
            }
            @Override
            public void act(Gesture g){
                Bar.this.decKey();
            }
        });

    }

    private void incKey() {
        if (key == null) {key = new Key();}
        if (key.glyph == Glyph.NATURAL) {
            key.glyph = Glyph.SHARP;
            key.n = 1;
            return;
        }
        if(key.glyph == Glyph.FLAT){key.glyph = Glyph.NATURAL; return;}// else was sharp key
        if (key.n < 7) { key.n ++;}
    }

    private void decKey() {
        if(key == null) {key = new Key();}
        if(key.glyph == Glyph.NATURAL) {key.glyph = Glyph.FLAT; key.n = -1; return;}
        if(key.glyph == Glyph.SHARP) {key.glyph = Glyph.NATURAL; return;}// else was flat key
        if(key.n > -7) {key.n--;}
    }

    public void show(Graphics g){
        //g.setColor(barType == 1 ? Color.RED : Color.BLACK);//for test
        //for (Staff staff : sys.staffs){g.drawLine(x, staff.yTop(), x, staff.yBot());} //STUB
        int sysTop = sys.yTop(), y1 = 0, y2 = 0; // y1,y2 mark top and bot of connected component
        boolean justSawBreak = true; // signals when we are at the top of a new connected component
        int nStaff = sys.staffs.size();
        for(int i = 0; i < nStaff; i++){
            Staff staff = sys.staffs.get(i);
            int staffTop = staff.yTop(); // top of this staff
            if(justSawBreak) {y1 = staffTop;} // remember start of connected component
            y2 = staff.yBot();  // remember bottom of this staff
            justSawBreak = !staff.fmt.barContinues;
            if(justSawBreak){ // have a connected component from y1 to y2
                drawLines(g, x, y1, y2);  // lines show only at end of connected components
            }
            if(barType > 3){drawDots(g, x, staffTop);} // dots on every staff w/ repeats
        }
        if (barType == 1 && key != null){
            key.drawOnSys(g, sys, x + UC.barKeyOffset);
        }
    }

    public void drawLines(Graphics g, int x, int y1, int y2){
        int H = sys.page.maxH;
        if(barType == 0){ thinBar(g, x, y1, y2);}
        if(barType == 1){ thinBar(g, x, y1, y2); thinBar(g, x - H, y1, y2);}
        if(barType == 2){ fatBar(g, x - H, y1, y2, H); thinBar(g, x - 2*H, y1, y2);}
        if(barType >= 4){
            fatBar(g, x - H, y1, y2, H); // all repeats have fat bar
            if((barType&LEFT) != 0){thinBar(g, x - 2*H, y1, y2); wings(g, x - 2*H, y1, y2, -H, H);}
            if((barType&RIGHT) != 0){thinBar(g, x + H, y1, y2); wings(g, x + H, y1, y2, H, H);}
        }
    }

    public void cycleType(){//cycle through bar line types (e.g., single, double, final)
        barType++;// Increment barType to switch to the next type
        if (barType > 2) {barType = 0;}
    }

    public void toggleLeft(){barType = barType^LEFT;} // XOR toggle the left-side thickness of the bar line
    public void toggleRight(){barType = barType^RIGHT;} //toggle the right-side thickness of the bar line, Use XOR (^) to toggle the RIGHT flag on/off

    public static void wings(Graphics g, int x, int y1, int y2, int dx, int dy){ //graphics helpers to draw different bar types
        g.drawLine(x, y1, x+dx, y1-dy);
        g.drawLine(x, y2, x+dx, y2+dy);
    }

    //drawing a thick vertical bar line. It fills a rectangular area to create a bold bar line in a music notation system.
    public static void fatBar(Graphics g, int x, int y1, int y2, int dx){g.fillRect(x, y1, dx, y2-y1);}
    public static void thinBar(Graphics g, int x, int y1, int y2){g.drawLine(x, y1, x, y2);}

    public void drawDots(Graphics g, int x, int top){
        // from top of single staff
        // notice - this code ASSUMES nLine is 5. We will need to fix if we ever allow
        // not-standard staffs.
        int H = sys.page.maxH;
        if((barType & LEFT) != 0){
            g.fillOval(x - 3*H, top + 11*H/4, H/2, H/2);
            g.fillOval(x - 3*H, top + 19*H/4, H/2, H/2);
        }
        if((barType & RIGHT) != 0){
            g.fillOval(x + 3*H/2, top + 11*H/4, H/2, H/2);
            g.fillOval(x + 3*H/2, top + 19*H/4, H/2, H/2);
        }
    }
}
