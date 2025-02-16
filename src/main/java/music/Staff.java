package music;

import graphics.*;
import reactions.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

import static music.MusicEd.PAGE;

public class Staff extends Mass {
    public Sys sys; // Every staff belongs to a system
    public int ndx; // index on staff list - basically sys.staffs.get(ndx) should be this staff

    public Clef.List clefs = null; // most staffs do NOT define clefs.
    public Staff.Fmt fmt;

    public Staff previousStaff(){
        Page p = (Page) sys.page; // Cast sys.page to Page
        return sys.ndx == 0 ? null : p.sysList.get(sys.ndx-1).staffs.get(ndx);
    }

    public Staff(Sys sys){
        super("BACK");
        this.sys = sys;
        this.ndx = sys.addStaff(this); // both adds the staff to the sys AND tells us where it was added.

        addReaction(new Reaction("S-S"){ // create Bar
            public int bid(Gesture g){
                int x = g.vs.xM(), y1 = g.vs.yL(), y2 = g.vs.yH();
                if(x < sys.page.left() || x > (sys.page.right() + UC.barToMarginSnap)){
                    return UC.noBid;
                }
                int d = Math.abs(y1 - Staff.this.yTop()) + Math.abs(y2 - Staff.this.yBot());
                return (d < 30) ? (d + 20) : UC.noBid; // bias of 20 allows Bar Cycle gesture to outbid this
            }
            public void act(Gesture g){
                new Bar(Staff.this.sys, g.vs.xM());
            }
        });

        addReaction(new Reaction("S-S"){ // toggle BarContinues
            public int bid(Gesture g){
                if(Staff.this.sys.ndx != 0){
                    return UC.noBid;
                } // we only change bar continues in first system
                int y1 = g.vs.yL(), y2 = g.vs.yH();
                if(Staff.this.ndx == sys.page.sysfmt().size() - 1){
                    return UC.noBid;
                } // last staff in sys can't continue
                if(Math.abs(y1 - Staff.this.yBot()) > 20){
                    return UC.noBid;
                }
                Staff nextStaff = sys.getStaff(ndx + 1);
                if(Math.abs(y2 - nextStaff.yTop()) > 20){
                    return UC.noBid;
                }
                return 10;
            }

            public void act(Gesture g){
                sys.page.sysfmt().get(Staff.this.ndx).toggleBarContinues();
            }
        });

        addReaction(new Reaction("SW-SW"){ // add Note to Staff
            public int bid(Gesture g){
                int x = g.vs.xM(), y = g.vs.yM();
                if(x < sys.page.left() || x > sys.page.right()){
                    return UC.noBid;
                }
                int H = Staff.this.H(), top = Staff.this.yTop() - H, bot = Staff.this.yBot() + H;
                if(y < top || y > bot){
                    return UC.noBid;
                }
                return 10;
            }
            public void act(Gesture g){
                new Head(Staff.this, g.vs.xM(), g.vs.yM());
            }
        });

        addReaction(new Reaction("W-S"){ // add Q Rest
            public int bid(Gesture g){
                int x = g.vs.xL(), y = g.vs.yM();
                if(x < sys.page.left() || x > sys.page.right()){
                    return UC.noBid;
                }
                int H = Staff.this.H(), top = Staff.this.yTop() - H, bot = Staff.this.yBot() + H;
                if(y < top || y > bot){
                    return UC.noBid;
                }
                return 10;
            }
            public void act(Gesture g){
                Time t = Staff.this.sys.getTime(g.vs.xL());
                new Rest(Staff.this, t);
            }
        });

        addReaction(new Reaction("E-S"){ // add E Rest
            public int bid(Gesture g){
                int x = g.vs.xL(), y = g.vs.yM();
                if(x < sys.page.left() || x > sys.page.right()){
                    return UC.noBid;
                }
                int H = Staff.this.H(), top = Staff.this.yTop() - H, bot = Staff.this.yBot() + H;
                if(y < top || y > bot){
                    return UC.noBid;
                }
                return 10;
            }
            public void act(Gesture g){
                Time t = Staff.this.sys.getTime(g.vs.xL());
                (new Rest(Staff.this, t)).nFlag = 1;
            }
        });

        addReaction(new Reaction("SW-SE"){ // add G Clef
            public int bid(Gesture g){
                int dTop = Math.abs(g.vs.yL() - yTop()), dBot = Math.abs(g.vs.yH() - yBot());
                if(dTop+dBot>60){return UC.noBid;}
                return dTop+dBot;
            }
            public void act(Gesture g){
                if(Staff.this.initialClef() == null){
                    setInitialClef(Glyph.CLEF_G);
                } else {
                    addNewClef(Glyph.CLEF_G, g.vs.xM());
                }
            }
        });

        addReaction(new Reaction("SE-SW"){ // add F Clef
            public int bid(Gesture g){
                int dTop = Math.abs(g.vs.yL() - yTop()), dBot = Math.abs(g.vs.yH() - yBot());
                if(dTop+dBot>60){return UC.noBid;}
                return dTop+dBot;
            }
            public void act(Gesture g){
                if(Staff.this.initialClef() == null){
                    setInitialClef(Glyph.CLEF_F);
                } else {
                    addNewClef(Glyph.CLEF_F, g.vs.xM());
                }
            }
        });

    }

    public void show(Graphics g){
        Clef clef = initialClef();
        int x = sys.page.left() + UC.initialClefOffset;
        if(clef != null){clef.glyph.showAt(g, fmt.H, x, lineOfY(4));}
    }


    public int yTop(){return sys.yTop() + sys.page.sysfmt().get(ndx).dy; }
    public int yBot(){ return yTop() + sys.page.sysfmt().get(ndx).height(); }
    public int yLine(int n){ return yTop() + n*H(); }

    public int lineOfY(int y){
        int H = sys.page.sysfmt().get(ndx).H;
        int Bias = 100;  // because integer truncation rounds toward 0 ..
        int top = yTop() - H*Bias;  // .. we move the origin to a number like -100
        return (y - top + H/2)/H - Bias; // .. calculate a big number then remove that Bias
    }

    public int H(){
        return sys.page.sysfmt().get(ndx).H;
    }

    public Clef lastClef(){return clefs == null ? null : clefs.get(clefs.size()-1);}
    public Clef firstClef(){return clefs == null ? null : clefs.get(0);}
    public Clef initialClef(){ // can return null if no clef has ever been set
        Staff s = this, ps = previousStaff();
        while(ps != null && ps.clefs == null){s = ps; ps = s.previousStaff();}
        return ps == null ? s.firstClef() : ps.lastClef();
    }

    public void setInitialClef(Glyph glyph){
        Staff s = this, ps = previousStaff();
        while(ps != null){s=ps; ps = s.previousStaff();} // find base of this staff
        s.clefs = new Clef.List();
        s.clefs.add(new Clef(s,-900,glyph)); // negative so doesn't show
    }

    public void addNewClef(Glyph glyph, int x){
        if(clefs == null){clefs = new Clef.List();}
        clefs.add(new Clef(this, x, glyph));
        Collections.sort(clefs);
    }

    public Glyph clefAtX(int x){ // can return null
        Clef iClef = initialClef();
        if(iClef == null){return null;}
        Glyph ret = iClef.glyph;
        if(clefs != null){
            for(Clef clef:clefs){if(clef.x <= x){ret = clef.glyph;}} // last clef before x
        }
        return ret;
    }

    //-------------------------------- Staff.Fmt ---------------------------------------
    public static class Fmt{
        public int H; // half the space between staff lines = thickness of whole and half rest
        public int nLines = 5; // most music is 5 lines, but there are exceptions
        public int dy; // technically a Sys.Fmt notion - how far from the sys.topY you should draw this staff.
        public boolean barContinues = false; // does bar though this staff continue onto the next staff

        public void toggleBarContinues(){
            barContinues = !barContinues;
        }

        public Fmt(int dy, I.Page page){
            this.dy = dy;
            this.H = page.sysfmt().maxH;
            page.sysfmt().add(this);
        }

        public void showAt(Graphics g, int y, I.Page page){
            for(int i = 0; i < nLines; i++) {
                int yy = y + 2*i*H;
                g.drawLine(page.left(), yy, page.right(), yy);
            }
        }

        public int height(){
            return 2*H*(nLines - 1);
        }
    }
}

/*public class Staff extends Mass{
    public Sys sys;        // the system that this staff lives in
    public int iStaff;     // the index of WHERE it lives in the system
    public G.HC staffTop;  // where the top of the staff will be on the screen.
    public Staff.Fmt fmt;  // the format used for drawing this staff
    public Clef clefs;

    public Staff(Sys sys, int iStaff, G.HC staffTop, Staff.Fmt fmt){
        super("BACK");
        this.fmt = fmt;
        this.sys = sys;
        this.iStaff = iStaff;
        this.staffTop = staffTop;

        addReaction(new Reaction("S-S"){ // create Bar line
            public int bid(Gesture g){
                Page PAGE = sys.page;
                int x = g.vs.xM(), y1 = g.vs.yL(), y2 = g.vs.yH();
                int left = PAGE.margins.left, right = PAGE.margins.right;
                if(x<left || x > (right + UC.barToMarginSnap) ){return UC.noBid;}
                int d = Math.abs(y1 - Staff.this.yTop()) + Math.abs(y2 - Staff.this.yBot());
                int bias = UC.barToMarginSnap; // max cycleBar bid which must outbid createBar
                return (d < 30)? (d + bias):UC.noBid;
            }
            public void act(Gesture g){
                new Bar(Staff.this.sys, g.vs.xM());
            }
        });

        addReaction(new Reaction("S-S"){ // toggle BarContinues
            public int bid(Gesture g){
                if(Staff.this.sys.iSys != 0){return UC.noBid;} // we only change bar continues in first system
                int y1 = g.vs.yL(), y2 = g.vs.yH();
                if(iStaff == sys.nStaff()-1 ){return UC.noBid;} // last staff in sys can't continue
                Staff nextStaff = sys.staffs.get(iStaff + 1);
                System.out.println("Staff"+iStaff+ " "+y1+":"+yBot()+ " - " + y2 +":"+ nextStaff.yTop());
                if(Math.abs(y1 - yBot()) > 20){return UC.noBid;}
                if(Math.abs(y2 - nextStaff.yTop()) > 20){return UC.noBid;}
                return 10;
            }
            public void act(Gesture g){
                fmt.toggleBarContinues();
            }
        });

        addReaction(new Reaction("SW-SW"){ // add Note to Staff
            public int bid(Gesture g){
                int x = g.vs.xM(), y = g.vs.yM();
                if(x < PAGE.margins.left || x > PAGE.margins.right) {return UC.noBid;}
                int H = Staff.this.h();
                int top = Staff.this.yTop()-H;
                int bot = Staff.this.yBot()+H;
                if(y < top || y > bot) {
                    return UC.noBid;
                }
                return 10;
            }
            @Override
            public void act(Gesture g){
                new Head(Staff.this, g.vs.xM(), g.vs.yM());
            }
        });

        addReaction(new Reaction("W-S"){ // add Q Rest
            @Override
            public int bid(Gesture g){
                int x = g.vs.xL(), y = g.vs.yM();
                if(x < sys.page.margins.left || x > sys.page.margins.right){return UC.noBid;}
                int H = fmt.H, top = yTop()-H,  bot = yBot()+H;
                if(y < top || y > bot){return UC.noBid;}
                return 10;
            }
            public void act(Gesture g){
                Time t = Staff.this.sys.getTime(g.vs.xL());
                new Rest(Staff.this, t);
            }
        });

        addReaction(new Reaction("E-S"){
            @Override
            public int bid(Gesture g){
                int x = g.vs.xL(), y = g.vs.yM();
                if(x< PAGE.margins.left || x > PAGE.margins.right){return UC.noBid;}
                int H = Staff.this.h();
                int top = Staff.this.yTop() - H;
                int bot = Staff.this.yBot() + H;
                if(y < top || y > bot){return UC.noBid;}
                return 10;
            }
            @Override
            public void act (Gesture g){
                Time t = Staff.this.sys.getTime(g.vs.xL());
                (new Rest(Staff.this, t)).nFlag = 1;
            }
        });

        addReaction(new Reaction("SW-SE"){ // add G Clef
            public int bid(Gesture g){
                int dTop = Math.abs(g.vs.yL() - yTop()), dBot = Math.abs(g.vs.yH() - yBot());
                if(dTop+dBot>60){return UC.noBid;}
                return dTop+dBot;
            }
            public void act(Gesture g){
                if(Staff.this.initialClef() == null){
                    setInitialClef(Glyph.CLEF_G);
                } else {
                    addNewClef(Glyph.CLEF_G, g.vs.xM());
                }
            }
        });

        addReaction(new Reaction("SE-SW"){ // add F Clef
            public int bid(Gesture g){
                int dTop = Math.abs(g.vs.yL() - yTop()), dBot = Math.abs(g.vs.yH() - yBot());
                if(dTop+dBot>60){return UC.noBid;}
                return dTop+dBot;
            }
            public void act(Gesture g){
                if(Staff.this.initialClef() == null){
                    setInitialClef(Glyph.CLEF_F);
                } else {
                    addNewClef(Glyph.CLEF_F, g.vs.xM());
                }
            }
        });
    }

    public Staff copy(Sys newSys){ // create copy of this for new system sys
        G.HC hc = new G.HC(newSys.staffs.sysTop, staffTop.dv);
        return new Staff(newSys, iStaff, hc, fmt);
    }

    public Clef lastClef(){return clefs == null ? null : clefs.get(clefs.size()-1);}
    public Clef firstClef(){return clefs == null ? null : clefs.get(0);}
    public Clef initialClef(){ // can return null if no clef has ever been set
        Staff s = this, ps = previousStaff();
        while(ps != null && ps.clefs == null){s = ps; ps = s.previousStaff();}
        return ps == null ? s.firstClef() : ps.lastClef();
    }

    public void setInitialClef(Glyph glyph){
        Staff s = this, ps = previousStaff();
        while(ps != null){s=ps; ps = s.previousStaff();} // find base of this staff
        s.clefs = new Clef();
        s.clefs.add(new Clef(s,-900,glyph)); // negative so doesn't show
    }

    public void addNewClef(Glyph glyph, int x){
        if(clefs == null){clefs = new Clef();}
        clefs.add(new Clef(this, x, glyph));
        Collections.sort(clefs);
    }

    public void show(Graphics g){
        Page.Margins m = sys.page.margins;
        int x1 = m.left, x2 = m.right, y = yTop(), h = fmt.H*2;
        for(int i=0; i<fmt.nLines; i++){g.drawLine(x1, y+i*h, x2, y+i*h);}
        Clef clef = initialClef();
        int x = sys.page.margins.left + UC.initialClefOffset;
        if(clef != null){clef.glyph.showAt(g, fmt.H, x, yOfLine(4));}
    }

    public int yTop(){return staffTop.v();} // top line of staff, what y represents
    public int yOfLine(int line){return yTop() + line*fmt.H;}
    public int yBot(){return yOfLine(2*(fmt.nLines-1));}
    public int h() {return fmt.H;}
    public int yLine(int n){return yTop() + n*h();}
    public int lineOfY(int y){
        int H = h();
        int Bias = 100;  // because integer truncation rounds toward 0
        int top = yTop() - H*Bias;  // we move the origin to a number like -100
        return (y-top +H/2)/H - Bias; // calculate a big number then remove that Bias
    }

    //-----------------STAFF FMT--------------------
    public static class Fmt{
        public static Fmt DEFAULT = new Fmt(5,8);
        public int nLines, H;
        public boolean barContinues = false;

        public Fmt(int nLines, int H){this.nLines = nLines; this.H = H;}
        public void toggleBarContinues(){barContinues = !barContinues;}
    }

    //--------------------STAFF.LIST-------------------
    public static class List extends ArrayList<Staff>{
        public G.HC sysTop;
        public List(G.HC sysTop){this.sysTop = sysTop;}
    }
}*/
