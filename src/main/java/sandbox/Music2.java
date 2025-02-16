package sandbox;

import graphics.*;
import music.*;
import reactions.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Music2 extends WinApp implements I.MusicApp{
    static{
        new Layer("BACK");
        new Layer("NOTE");
        new Layer("FORE");
    }

    public Sys.Fmt sysfmt(I.Page page){return SYSFMT;}
    public ArrayList<Sys> systems(I.Page page){return SYSTEMS;}
    public ArrayList<I.Page> pages(){return PAGES;}

    public static class M2Page implements I.Page{
        public int top = 50;
        public int top(){ return top; }
        public int left(){ return 50; }
        public int right(){ return UC.mainWindowWidth - 50; }
        public int bot(){return UC.mainWindowHeight - 50; }
        public Sys.Fmt sysfmt(){ return SYSFMT; }
        public ArrayList<Sys> systems(){return SYSTEMS; }
    }

    public static I.Page PAGE = new M2Page();
    public static Sys.Fmt SYSFMT; // set by initialAct
    public static ArrayList<Sys> SYSTEMS = new ArrayList<>();
    public static ArrayList<I.Page> PAGES = new ArrayList<>();
    static{PAGES.add(PAGE);}  // this app runs with a single page

    public Music2(){
        super("Music 2 - Layout", UC.mainWindowWidth, UC.mainWindowHeight);
        APP.get = this; // make this application accessible to the music classes.
        Reaction.initialAct = new I.Act(){
            public void act(Gesture g){
                SYSFMT = null;
            }
        };

        Reaction.initialReactions.addReaction(new Reaction("E-E"){ // add New Staff to SYSFMT
            public int bid(Gesture g){
                if(SYSFMT == null){
                    return 0;
                } // always bid for E-E on an empty page
                int y = g.vs.yM();
                if(y > PAGE.top() + SYSFMT.height() + 15){
                    return 100;
                } // only bid if y is below last staff in first sys
                return UC.noBid;
            }

            public void act(Gesture g){
                int y = g.vs.yM();
                if(SYSFMT == null){
                    ((M2Page)PAGE).top = y;
                    SYSFMT = new Sys.Fmt();
                    SYSTEMS.clear();
                    new Sys(PAGE);
                }
                SYSFMT.addNewStaff(PAGE, y);
            }
        });

        Reaction.initialReactions.addReaction(new Reaction("E-W"){ // SYSFMT finished, add new System.
            public int bid(Gesture g){
                if(SYSFMT == null){
                    return UC.noBid;
                } // don't add sys if there is no SYSFMT yet
                int y = g.vs.yM();
                if(y > SYSTEMS.get(SYSTEMS.size() - 1).yBot() + 15){
                    return 100;
                } //..if y is BELOW last System
                return UC.noBid;
            }

            public void act(Gesture g){
                int y = g.vs.yM();
                if(SYSTEMS.size() == 1){ // when first system is finished we are defining sysGap
                    SYSFMT.sysGap = y - (PAGE.top() + SYSFMT.height());
                    //PAGE.nSys = (PAGE.bot() - PAGE.top())/(SYSFMT.height() + PAGE.sysGap); // unused in this app
                    //System.out.println("nSys: " + PAGE.nSys);
                }
                new Sys(PAGE);
            }
        });
    }//----end of Music 2 constructor

    public void paintComponent(Graphics g){
        G.clearScreen(g);
        g.setColor(Color.BLACK);
        Ink.BUFFER.show(g);
        Layer.ALL.show(g);
        int xa = 100, ya = 300;
        int xb = xa + 50 + G.rnd(300), yb = ya + G.rnd(2000)-1000;
        int xc = xa +500, yc =ya ;
        G.poly.reset();
        G.pSpline(xa,ya,xb,yb,xc,yc,4);
        g.fillPolygon(G.poly);
    }

    public void mousePressed(MouseEvent me){
        Gesture.AREA.dn(me.getX(), me.getY());
        repaint();
    }

    public void mouseDragged(MouseEvent me){
        Gesture.AREA.drag(me.getX(), me.getY());
        repaint();
    }

    public void mouseReleased(MouseEvent me){
        Gesture.AREA.up(me.getX(), me.getY());
        repaint();
    }
}
