package music;

import graphics.*;
import reactions.*;
import reactions.Shape;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class MusicEd extends WinApp {
    //public Layer BACK = new Layer("BACK"), FORE = new Layer("FORE");
    static{new Layer("BACK"); new Layer("NOTE"); new Layer("FORE");}//call new layer
    public static boolean training = false;
    public static I.Area curArea = Gesture.AREA; //switch between training and gesture at any time
    public static Page PAGE; //single page

    public MusicEd() {
        super("Music Editor", UC.mainWindowWidth, UC.mainWindowHeight);
        Reaction.initialReactions.addReaction(new Reaction("W-W") {//define top margin
            public int bid(Gesture g){return 0;}
            public void act(Gesture g) {
                int y = g.vs.yM();
                PAGE = new Page(y);
                this.disable(); //locate to Page
            }
        });
    }

    //static int[] xPoly = {100, 200, 200, 100};
    //static int[] yPoly = {50, 70, 80, 60};
    //static Polygon poly = new Polygon(xPoly, yPoly, 4);

    public void paintComponent(Graphics g) {
        G.clearScreen(g);
        if(training) {Shape.TRAINER.show(g); return;}
        Layer.ALL.show(g);
        g.setColor(Color.BLACK);
        Ink.BUFFER.show(g);
        g.drawString(Gesture.recognized, 900, 30);
        if (PAGE != null){//which char in font, scale, x, y;
            //Staff staff = PAGE.sysList.get(0).staffs.get(0);//test key signatures
            //Key.drawOnStaff(g, 7, Key.fF, 110, Glyph.FLAT, staff);//can change Key.fF and Glyph.FLAT for test
            //Glyph.CLEF_G.showAt(g, 8, 100, PAGE.margins.top + 4*8);
            //Glyph.HEAD_Q.showAt(g, 8, 200, PAGE.margins.top + 4*8);// y - center line
            //draw boxes to get font size
            //int H = 32;
//            Glyph.HEAD_Q.showAt(g, H, 200, PAGE.margins.top + 4*H);
//            g.setColor(Color.RED);
//            g.drawRect(200, PAGE.margins.top + 3*H, 24*H/10, 24*H/10);

        }

        ////Test - draw Poly
        //g.fillPolygon(poly);//test-draw polygons
        //poly.ypoints[3]++;//test-run and shake the window

        ////Test - draw Polygon
        //Beam.setPoly(100, 100+G.rnd(100), 200, 100+G.rnd(100), 8);
        //g.fillPolygon(Beam.poly);

        ////Test-draw beam stack
//        int H = 8, x1 = 100, x2 = 200;
//        Beam.setMasterBeam(x1, 100+G.rnd(100), x2, 100+G.rnd(100));
//        g.drawLine(0, Beam.my1, x1, Beam.my1);//debug
//        Beam.drawBeamStack(g, 0, 1, x1, x2, H);
//        g.setColor(Color.ORANGE);
//        Beam.drawBeamStack(g, 1, 3, x1 + 10, x2 - 10, H);
    }

    public void mousePressed(MouseEvent me) { curArea.dn(me.getX(), me.getY()); repaint();}
    public void mouseDragged(MouseEvent me) { curArea.drag(me.getX(), me.getY()); repaint();}
    public void mouseReleased(MouseEvent me) {
        curArea.up(me.getX(), me.getY());
        trainBtn(me);
        repaint();
    }

    public void keyTyped(KeyEvent ke) {if(training) {Shape.TRAINER.keyTyped(ke); repaint();}}
    public void trainBtn(MouseEvent me) {//train button
        if(me.getX() > (UC.mainWindowWidth-40) && (me.getY() < 40)){
            training = !training; //toggle training
            curArea = training ? Shape.TRAINER : Gesture.AREA;
        }
    }

    public static void main(String[] args) {
        PANEL = new MusicEd();
        WinApp.launch();
    }
}
