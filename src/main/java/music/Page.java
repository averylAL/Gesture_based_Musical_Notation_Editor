package music;

import graphics.*;
import reactions.*;

import java.awt.*;

public class Page extends Mass {
    public Margins margins = new Margins();
    public int sysGap;
    public G.HC pageTop;
    public Sys.List sysList;
    public int maxH = 0;

    public Page(int y){
        super("BACK");
        margins.top = y;
        pageTop = new G.HC(G.HC.ZERO, y);// y is how far down
        G.HC sysTop = new G.HC(pageTop, 0);// locate the top of first system
        sysList = new Sys.List();
        sysList.add(new Sys(this, sysTop));//create first system on page
        updateMaxH();

        // add reactions below
        addReaction(new Reaction("W-W") {//add newStaff to first system
            public int bid(Gesture g){//only allow add staffs in first new system
                if(sysList.size()!=1){return UC.noBid;}
                Sys sys = sysList.get(0);
                int y = g.vs.yM();
                //System.out.println("y = " + y);
                if(y<sys.yBot()+UC.minStaffGap){return UC.noBid;}
                return 1000;
            }
            public void act(Gesture g) {
                sysList.get(0).addNewStaff(g.vs.yM());
            }
        });

        addReaction(new Reaction("W-E") {//add new system to Page
            public int bid(Gesture g){
                Sys lastSys = sysList.get(sysList.size()-1);
                int y = g.vs.yM();
                if(y< lastSys.yBot()+UC.minSysGap){return UC.noBid;}
                return 1000;
            }
            public void act(Gesture g) {
                addNewSys(g.vs.yM());
            }
        });
    }

    public void updateMaxH(){
        Sys sys = sysList.get(0);
        int newH = sys.staffs.get(sys.staffs.size() - 1).fmt.H;
        if (maxH < newH) {maxH = newH;}
    }

    public void addNewSys(int y){
        int nSys = sysList.size(), sysHeight = sysList.get(0).height();
        if(nSys == 1){// "nSys == 1" means second system we add
            //System.out.println("Setting sys gap"+ y + " " + sysHeight + " " + pageTop.v());//debug
            sysGap = y-sysHeight-pageTop.v();
        }
        // calculate new HC for top of the new system
        G.HC sysTop = new G.HC(pageTop, nSys*(sysHeight+sysGap));
        //System.out.println("Add new sys" + sysTop.v());//debug
        //System.out.println("nSys: " + nSys + ", sysHeight: " + sysHeight + ", sysGap: " + sysGap);//debug
        sysList.add(new Sys(this, sysTop));
    }

    public void show(Graphics g){g.setColor(Color.BLACK);}

    //------------------------------------MARGINS-----------------------------
    public static class Margins {
        private static int MM=50; //magic number
        public int top = MM, left = MM, bot = UC.mainWindowHeight-MM, right = UC.mainWindowWidth-MM;
    }

}// end of class Page
