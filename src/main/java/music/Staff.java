package music;

import graphics.*;
import reactions.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Staff extends Mass {
    public Sys sys;
    public int iStaff; //the index of where it lives in the dad's list
    public G.HC staffTop; // where the top of the staff will be on the screen
    public Staff.Fmt fmt; //the format of staff
    public Clef.List clefs = null; //most staffs do not define clefs

    public Staff(Sys sys, int iStaff, G.HC staffTop, Staff.Fmt fmt) {
        super("BACK"); // which layer
        this.sys = sys;
        this.iStaff = iStaff;
        this.staffTop = staffTop;
        this.fmt = fmt;

        addReaction(new Reaction("S-S") {// create Bar line
            @Override
            public int bid(Gesture g) {
                Page PAGE = sys.page;
                int x = g.vs.xM(), y1 = g.vs.yL(), y2 = g.vs.yH();
                int left = PAGE.margins.left, right = PAGE.margins.right;
                if (x < left || x > (right + UC.barToMarginSnap)){return UC.noBid;} // deal with right of the staff
                //if (x < PAGE.margins.left || x > PAGE.margins.right) {return UC.noBid;}
                int d = Math.abs(y1 - yTop()) + Math.abs(y2 - yBot());
                int bias = UC.barToMarginSnap; //maximum cycle bar bid must outbid create bar
                return d < 30 ? d + bias : UC.noBid;
            }
            @Override
            public void act(Gesture g) {
                new Bar(Staff.this.sys, g.vs.xM());
            }
        });

        addReaction(new Reaction("S-S"){ // toggle BarContinues
            @Override
            public int bid(Gesture g){
                if(Staff.this.sys.iSys != 0){return UC.noBid;} // only change bar continues in first system
                int y1 = g.vs.yL(), y2 = g.vs.yH();
                if(iStaff == sys.staffs.size()-1) {return UC.noBid;} // last staff in sys can't continue
                if(Math.abs(y1 - yBot()) > 20){return UC.noBid;}
                Staff nextStaff = sys.staffs.get(iStaff + 1);
                if(Math.abs(y2 - nextStaff.yTop()) > 20){return UC.noBid;} //too far from next top
                //System.out.println("Staff"+iStaff+ " "+y1+":"+yBot()+ " - " + y2 +":"+ nextStaff.yTop());//debug
                return 10; //low value
            }
            @Override
            public void act(Gesture g){
                fmt.toggleBarContinues();
            }
        });

        addReaction(new Reaction("SW-SW") {//add Note to staff, click top right corner to train first
            @Override
            public int bid(Gesture g) {
                int x = g.vs.xM(), y = g.vs.yM();
                if(x < sys.page.margins.left || x > sys.page.margins.right){return UC.noBid;}
                int H = fmt.H;//Staff.this.H()
                int top = yTop() - H, bot = yBot() + H;
                if(y < top || y > bot){return UC.noBid;}
                return 10;
            }
            @Override
            public void act(Gesture g) {
                new Head(Staff.this, g.vs.xM(), g.vs.yM());
            }
        });

        addReaction(new Reaction("W-S") {//add Q Rest
            @Override
            public int bid(Gesture g) {
                int x = g.vs.xL(), y = g.vs.yM();
                if(x < sys.page.margins.left || x > sys.page.margins.right){return UC.noBid;}
                int H = fmt.H, top = yTop() - H, bot = yBot() + H;
                if(y < top || y > bot){return UC.noBid;}
                return 10;
            }
            @Override
            public void act(Gesture g) {
                Time t = Staff.this.sys.getTime(g.vs.xL());
                new Rest(Staff.this, t);
            }
        });

        addReaction(new Reaction("E-S") {//add Eighth Rest
            @Override
            public int bid(Gesture g) {
                int x = g.vs.xL(), y = g.vs.yM();
                if(x < sys.page.margins.left || x > sys.page.margins.right){return UC.noBid;}
                int H = fmt.H, top = yTop() - H, bot = yBot() + H;
                if(y < top || y > bot){return UC.noBid;}
                return 10;
            }
            @Override
            public void act(Gesture g) {
                Time t = Staff.this.sys.getTime(g.vs.xL());
                (new Rest(Staff.this, t)).nFlag = 1;
            }
        });

        addReaction(new Reaction("SW-SE") {//add G Clef
            @Override
            public int bid(Gesture g) {
                int dTop = Math.abs(g.vs.yL() - yTop());//delta top, HOW CLOSE TO TOP
                int dBot = Math.abs(g.vs.yH() - yBot());
                System.out.println("dTop: " + dTop + ", dBot: " + dBot);//debug
                if((dTop + dBot) > 60){return UC.noBid;}
                System.out.println("Winner!");//debug
                return dTop + dBot;
            }
            @Override
            public void act(Gesture g) {
                if(Staff.this.initialClef() == null){
                    setInitialClef(Glyph.CLEF_G);
                } else {addNewClef(Glyph.CLEF_G, g.vs.xM());}
            }
        });

        addReaction(new Reaction("SE-SW") {//add F Clef
            @Override
            public int bid(Gesture g) {
                int dTop = Math.abs(g.vs.yL() - yTop());//delta top, HOW CLOSE TO TOP
                int dBot = Math.abs(g.vs.yH() - yBot());
                if((dTop + dBot) > 60){return UC.noBid;}
                return dTop + dBot;
            }
            @Override
            public void act(Gesture g) {
                if(Staff.this.initialClef() == null){
                    setInitialClef(Glyph.CLEF_F);
                } else {addNewClef(Glyph.CLEF_F, g.vs.xM());}
            }
        });
    }

    public Staff previousStaff(){
        return sys.iSys == 0 ? null : sys.page.sysList.get(sys.iSys - 1).staffs.get(iStaff);
    }
    public Clef lastClef(){return clefs == null ? null : clefs.get(clefs.size() - 1);}
    public Clef firstClef(){return clefs == null ? null : clefs.get(0);}
    public Clef initialClef(){//can return null if no clef has ever been set
        Staff s = this, ps = previousStaff();
        while(ps != null && ps.clefs == null){s = ps; ps = s.previousStaff();}
        return ps == null ? s.firstClef() : ps.lastClef();
    }
    public void setInitialClef(Glyph glyph){
        Staff s = this, ps = previousStaff();
        while(ps != null){s = ps; ps = s.previousStaff();}//find first staff
        s.clefs = new Clef.List();
        s.clefs.add(new Clef(s,-900, glyph));
    }
    public void addNewClef(Glyph glyph, int x){
        if(clefs == null){clefs = new Clef.List();}
        clefs.add(new Clef(this, x, glyph));
        Collections.sort(clefs);//clefs sorted by x value
    }

    public int yTop(){return staffTop.v();}
    public int yOfLine(int line){return yTop() + line*fmt.H;} //which line that put note; H is half space between lines
    public int yBot(){return yOfLine(2*(fmt.nLines-1));}//if 5 lines, we have 4 spaces

    public int yLine(int n){return yTop() + n*fmt.H;}
    public int lineOfY(int y){
        int H = fmt.H;
        int Bias = 100;//bkz integer truncation rounds toward 0
        int top = yTop() - H*Bias;//move the origin to a number like -100
        return (y - top + H/2)/H - Bias;//remove bias
    }

    public Staff copy(Sys newSys){
        G.HC hc=new G.HC(newSys.staffs.sysTop, staffTop.dv);
        return new Staff(newSys, iStaff, hc, fmt);
    }

    public void show(Graphics g){
        Page.Margins m=sys.page.margins;
        int x1=m.left, x2=m.right, y=yTop(), h=fmt.H*2;
        for (int i=0; i<fmt.nLines; i++){
            g.drawLine(x1, y+i*h, x2, y+i*h);
        }
        Clef clef = initialClef();
        int x = sys.page.margins.left + UC.initialClefOffset;
        if(clef != null){clef.glyph.showAt(g, fmt.H, x, yOfLine(4));}
    }

    public Glyph clefAtX(int x) {
        Clef iClef = initialClef();
        if(iClef == null){return null;}
        Glyph res = iClef.glyph;
        if(clefs != null){
            for(Clef clef : clefs){
                if(clef.x <= x){res = clef.glyph;}
            }
        }
        return res;
    }

    //------------------------------Staff.FORMAT--------------------------------------
    public static class Fmt{
        public int nLines, H; //nLines represents how many lines in this staff
        public boolean barContinues = false;
        public Fmt(int nLines, int H){this.nLines = nLines;this.H = H;}
        public void toggleBarContinues(){barContinues = !barContinues;}
    }

    //-----------------------------Staff.List------------------------------------
    public static class List extends ArrayList<Staff> {
        public G.HC sysTop;
        public List(G.HC sysTop){this.sysTop = sysTop;}
    }

}
