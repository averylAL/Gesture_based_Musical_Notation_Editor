package music;

import graphics.*;
import reactions.*;
import java.awt.*;
import java.util.ArrayList;

public class Page extends Mass{
    public Margins margins = new Margins();
    public int sysGap; // size of spacing between Sys on page
    public G.HC pageTop;
    public ArrayList<Sys> sysList = new ArrayList<>();
    public int maxH = 0; // maximum H value in all Staffs in the system
    public ArrayList<Sys> systems = new ArrayList<>();
    public Sys.Fmt sysfmt;
    // Page Constructor
    public Page(Sys.Fmt sysfmt) {
        super("SomeTitle");
        this.sysfmt = sysfmt;
    }
    public int addSys(Sys sys){systems.add(sys); return systems.size()-1;}

    public Page(int y){
        super("BACK");
        // set up yFirstSys and create the first Sys which will create the first staff
        margins.top = y;
        pageTop = new G.HC(G.HC.ZERO, y);
        G.HC sysTop = new G.HC(pageTop,0);// locate the top of first system
        sysList.add(new Sys((I.Page) this));// create first system on page.
        updateMaxH();

        addReaction(new Reaction("W-W"){ // add newStaff to FirstSys only works if exactly 1 sys
            public int bid(Gesture g){
                // only first system gets to bid and only if it is the only system
                if(sysList.size() != 1){return UC.noBid;}
                Sys sys = sysList.get(0);
                int y = g.vs.yM();
                if(y < sys.yBot() + UC.minStaffGap){return UC.noBid;}
                return 1000;
            }
            public void act(Gesture g){
                sysList.get(0).addNewStaff(g.vs.yM());
            }
        });

        addReaction(new Reaction("W-E"){ // add new Sys to Page
            public int bid(Gesture g){
                // only last sys gets to bid
                //if(iSys != (page.sysList.size() - 1)){return UC.noBid;}
                Sys lastSys = sysList.get(sysList.size()-1);
                int y = g.vs.yM();
                if(y < lastSys.yBot() + UC.minSysGap) {return UC.noBid;}
                return 1000;
            }
            public void act(Gesture g){
                addNewSys(g.vs.yM());
            }
        });
    }

    public void addNewSys(int y){ // called by page, so safe to assume 1 Sys already
        int sysHeight = sysList.get(0).page.sysfmt().height(), nSys = sysList.size();
        if(nSys == 1){ // second sys defines sysGap
            sysGap = y - sysHeight - pageTop.v();
        }
        // calculate a new HC for the top of the new sys
        G.HC sysTop = new G.HC(pageTop, nSys*(sysHeight+sysGap));
        sysList.add(new Sys((I.Page) this));
    }

    public void show(Graphics g){g.setColor(Color.BLACK);} // sets color for staff drawing

    public void updateMaxH(){
        Sys sys = sysList.get(0);
        int newH = sys.staffs.get(sys.staffs.size()-1).fmt.H; // H from recent staff addition
        if(maxH<newH) {maxH=newH;}
    }

    //-----------------Margins--------------------------
    public static class Margins{
        private static final int M = 50;
        public int top = M, left = M;
        public int bot = UC.mainWindowHeight - M;
        public int right = UC.mainWindowHeight - M;
    }
}
