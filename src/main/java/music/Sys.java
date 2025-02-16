package music;

import graphics.*;
import reactions.*;
import java.awt.*;
import java.util.ArrayList;

public class Sys extends Mass{
    public I.Page page;
    public int ndx;
    int x;
    ArrayList<Staff> staffs; // my y value is hidden in my list of staffs.
    public Time.List times;
    public Stem.List stems = new Stem.List();
    public Key initialKey = new Key();

    public Sys(I.Page page){
        super("BACK");
        this.page = page;
        staffs = new ArrayList<Staff>();
        ArrayList<Sys> systems = APP.get.systems(page);
        systems.add(this);
        ndx = systems.size()-1;
        makeStaffsMatchSysfmt(); // populate the staffs list
        times = new Time.List(this);

        addReaction(new Reaction("E-E"){ // Beam Stems.
            public int bid(Gesture g){
                int x1 = g.vs.xL(), y1 = g.vs.yL(), x2 = g.vs.xH(), y2 = g.vs.yH(); // collect the gesture numbers

                if(stems.fastReject(y1, y2)) {return UC.noBid;} // reject if gesture does not overlap the list of stems
                ArrayList<Stem> temp = stems.allIntersectors(x1,y1,x2,y2); // possible overlap: find intersecting stems
                if(temp.size() < 2) {return UC.noBid;} // crossing a single stem is a Stem reaction, not a Sys reaction
                System.out.println("Crossed "+temp.size()+" stems"); // debug

                Beam b = temp.get(0).beam; //check if all crossed stems are owned by the same beam (including null!)
                for(Stem s : temp){if(s.beam != b) {return UC.noBid;}} // different owners is reject
                System.out.println("all stems share owner"); // debug

                if(b == null && temp.size() != 2) {return UC.noBid;} // only new Beam if exactly 2 null beamed stems
                if(b==null && (temp.get(0).nFlag != 0 || temp.get(1).nFlag != 0)) {return UC.noBid;}
                return 50;
            }
            public void act(Gesture g){
                int x1 = g.vs.xL(), y1 = g.vs.yL(), x2 = g.vs.xH(), y2 = g.vs.yH();
                ArrayList<Stem> temp = stems.allIntersectors(x1,y1,x2,y2);
                Beam b = temp.get(0).beam;
                if(b == null){
                    new Beam(temp.get(0), temp.get(1));
                }else{
                    for(Stem s : temp){s.incFlag();}
                }
            }
        });

        addReaction(new Reaction("W-W"){
            public int bid(Gesture g){
                int x = page.left();
                int x1 = g.vs.xL(), x2 = g.vs.xH(); // Bar.this.x == x
                if(x1 > x || x2 < x){return UC.noBid;}
                int y = g.vs.yM();
                if(y < yTop() || y > yBot()){return UC.noBid;}
                return Math.abs(x - (x1+x2)/2); // how far is gesture midpoint from x
            }
            public void act(Gesture g){
                Sys.this.decKey();
            }
        });
    }

    void makeStaffsMatchSysfmt(){
        while(staffs.size() < APP.get.sysfmt(page).size()) {
            new Staff(this);
        }
    }

    public Time getTime(int x){
        return times.getTime(x);
    }

    public void show(Graphics g){
        int yTop = yTop(), yBot = yBot();
        //System.out.println("Sys:" + this + " y=" + yTop);
        g.setColor(Color.BLACK);
        APP.get.sysfmt(page).showAt(g, yTop, page);
        g.drawLine(page.left(), yTop, page.left(), yBot); // draw line grouping all staffs in sys
        stems.showMinMax(g);
        int xKey = x + UC.marginKeyOffset;
        initialKey.drawOnSys(g,this,xKey);
    }

    public int yTop(){
        return page.top() + ndx*(page.sysfmt().height() + page.sysfmt().sysGap);
    }

    public int yBot(){
        return yTop() + page.sysfmt().height();
    }

    public void addTime(Time time) {times.add(time);}
    public int addStaff(Staff staff) {staffs.add(staff); return staffs.size()-1;}
    public Staff getStaff(int ndx) {return staffs.get(ndx);}
    public void addStem(Stem s) {stems.addStem(s);}
    public void removeStem(Stem s) {stems.removeStem(s);}

    public void incKey(){
        if(initialKey.n<7){initialKey.n++;}
        initialKey.glyph = initialKey.n >= 0 ? Glyph.SHARP : Glyph.FLAT;
    }
    public void decKey(){
        if(initialKey.n>-7){initialKey.n--;}
        initialKey.glyph = initialKey.n >= 0 ? Glyph.SHARP : Glyph.FLAT;
    }

    public void addNewStaff(int y) {
        page.sysfmt().addNewStaff(page, y);
    }


    //----------------------------Sys.Fmt--------------------------------------------
    public static class Fmt extends ArrayList<Staff.Fmt>{
        public int maxH = UC.defaultStaffH;
        public int sysGap = 0; // set by gesture when second system is added to a page.

        public void showAt(Graphics g, int y, I.Page page){
            for(Staff.Fmt sf : this) {
                sf.showAt(g, y + sf.dy, page);
            } // show all the staffs at the proper delta
        }

        public int height(){
            Staff.Fmt last = get(size() - 1);
            return last.dy + last.height();
        }

        public Staff.Fmt getLast(){
            return get(size() - 1);
        }


        public void addNewStaff(I.Page page, int y){
            new Staff.Fmt(y - page.top(), page); // first create the Staff.Fmt which will join the SYSFMT list
            for(I.Page p: APP.get.pages()) {
                if(p.sysfmt() == page.sysfmt()){
                    for(Sys s : p.systems()) {
                        s.makeStaffsMatchSysfmt();
                    }
                }
            }
            ; // every existing system must be updated
        }
    }
}
