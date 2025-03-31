package music;

import graphics.*;
import reactions.*;

import java.awt.*;
import java.util.ArrayList;

public class Sys extends Mass {
    public Page page;
    public int iSys;
    public Staff.List staffs;//y value in staff.list
    public Time.List times;
    public Stem.List stems = new Stem.List();
    public Key initialKey = new Key();

    public Sys(Page page, G.HC sysTop){
        super("BACK");
        this.page = page;
        iSys = page.sysList.size();
        staffs = new Staff.List(sysTop);
        times = new Time.List(this);

        if(iSys == 0){// first system we created
            staffs.add(new Staff(this, 0, new G.HC(sysTop, 0), new Staff.Fmt(5, 8)));// sys itself, 0, G.HC
        } else {//other systems are clones of first system
            Sys oldSys = page.sysList.get(0);
            for (Staff oldStaff : oldSys.staffs){
                Staff ns = oldStaff.copy(this);//ns is new system
                this.staffs.add(ns);
            }
        }

        addReaction(new Reaction("E-E"){ //Beam two Stems.
            @Override
            public int bid(Gesture g){
                int x1 = g.vs.xL(), y1 = g.vs.yL(), x2 = g.vs.xH(), y2 = g.vs.yH(); // collect the gesture numbers
                if(stems.fastReject((y1 + y2)/2)){return UC.noBid;} // reject if gesture does not overlap the list of stems
                ArrayList<Stem> temp = stems.allIntersectors(x1, y1, x2, y2); // possible overlap: find intersecting stems
                if(temp.size() < 2){return UC.noBid;} // crossing a single stem is a Stem reaction, not a Sys reaction
                System.out.println("Crossed "+temp.size()+" stems"); // debug
                Beam b = temp.get(0).beam; //check if all crossed stems are owned by the same beam (including null)
                for(Stem s : temp){// different owners is rejected
                    if(s.beam != b){ return UC.noBid; }
                }
                //System.out.println("all stems share owner"); //debug
                if(b == null && temp.size() != 2){ return UC.noBid; } // only new Beam if exactly 2
                if(b == null && (temp.get(0).nFlag != 0 || temp.get(1).nFlag != 0)){ return UC.noBid; } // only new if both are zero nFlag
                return 50; // either create a new Beam or flags a set of beams
            }
            @Override
            public void act(Gesture g){
                int x1 = g.vs.xL(), y1 = g.vs.yL(), x2 = g.vs.xH(), y2 = g.vs.yH();
                ArrayList<Stem> temp = stems.allIntersectors(x1, y1, x2, y2);
                Beam b = temp.get(0).beam;
                if(b == null){
                    new Beam(temp.get(0), temp.get(1));
                }else{
                    for(Stem s : temp){s.incFlag();}
                }
            }
        });

        addReaction(new Reaction("E-E"){ //increment initial key.
            @Override
            public int bid(Gesture g){
               int x = page.margins.left, x1 = g.vs.xL(), x2 = g.vs.xH();
               if (x1 > x || x2 < x) {return UC.noBid;}
               int y = g.vs.yM();
               if (y < yTop() || y > yBot()) {return UC.noBid;}
               return Math.abs(x - (x1 + x2)/2);
            }
            @Override
            public void act(Gesture g){
                Sys.this.incKey();
            }
        });

        addReaction(new Reaction("W-W"){ //decrement initial key.
            @Override
            public int bid(Gesture g){
                int x = page.margins.left, x1 = g.vs.xL(), x2 = g.vs.xH();
                if (x1 > x || x2 < x) {return UC.noBid;}
                int y = g.vs.yM();
                if (y < yTop() || y > yBot()) {return UC.noBid;}
                return Math.abs(x - (x1 + x2)/2);
            }
            @Override
            public void act(Gesture g){
                Sys.this.decKey();
            }
        });
    }

    private void incKey() {
        if (initialKey.n < 7) {initialKey.n++;}
        initialKey.glyph = (initialKey.n >= 0 ? Glyph.SHARP : Glyph.FLAT);
    }

    private void decKey() {
        if (initialKey.n > -7) {initialKey.n--;}
        initialKey.glyph = initialKey.n >= 0 ? Glyph.SHARP : Glyph.FLAT;
    }

    public Time getTime(int x){return times.getTime(x);}

    public int yTop(){return staffs.sysTop.v();}
    public int yBot(){return staffs.get(staffs.size()-1).yBot();}
    public int height(){return yBot()-yTop();}

    public void show(Graphics g){
        int x = page.margins.left;
        g.drawLine(x, yTop(), x, yBot());
        int xKey = x + UC.marginKeyOffset;
        initialKey.drawOnSys(g, this, xKey);
    }

    public void addNewStaff(int y){
        int offSet = y-staffs.sysTop.v();
        G.HC staffTop = new G.HC (staffs.sysTop, offSet);
        staffs.add(new Staff(this, staffs.size(), staffTop, new Staff.Fmt(5, 8)));
        page.updateMaxH();
    }

    //-----------------------------Sys.List----------------------------------------------
    public static class List extends ArrayList<Sys> {}

}//end of Sys class
