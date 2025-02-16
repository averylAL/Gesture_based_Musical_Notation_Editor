package music;

import graphics.*;
import reactions.*;
import reactions.Gesture;
import reactions.Ink;
import reactions.Layer;
import reactions.Shape;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class MusicEd extends WinApp{
    public Layer BACK = new Layer("BACK"), FORE = new Layer("FORE");
    public static boolean training = false;
    public static I.Area curArea = Gesture.AREA; //  Gestures or Training at any time

    public static Page PAGE; // single page app

    public MusicEd() {
        super("Music Editor", UC.mainWindowWidth, UC.mainWindowHeight);
        Reaction.initialReactions.addReaction(new Reaction("E-E"){ // define top margin
        public int bid(Gesture g){return 0;}
        public void act(Gesture g){
            int y = g.vs.yM();
            PAGE = new Page(y);
//            System.out.println("PAGE initialized at y = " + y);//debug
            this.disable();
        }
    });
    }

    public void paintComponent(Graphics g) {
        G.clearScreen(g);
        if (training) {
            Shape.TRAINER.show(g);
            return;
        }
        Ink.BUFFER.show(g);
        Layer.ALL.show(g);
        g.drawString(Gesture.recognized, 900,30);
//        if (PAGE != null) {
//            //System.out.println("PAGE.margins.top = " + PAGE.margins.top);//debug
//            Glyph.CLEF_G.showAt(g, 8, 100, PAGE.margins.top + 4 * 8);
//            Glyph.HEAD_HALF.showAt(g, 8, 200, PAGE.margins.top + 4 * 8);
//            Glyph.HEAD_Q.showAt(g, 8, 200, PAGE.margins.top + 4 * 8);
//        } else {
//            System.out.println("PAGE is NULL!");
//        }
//        g.drawString(Gesture.recognized, 900, 30);
//        int H = 32;
//        Glyph.HEAD_Q.showAt(g, H, 200, PAGE.margins.top + 4*H);
//        g.setColor(Color.RED);
//        g.drawRect(200,PAGE.margins.top + 3*H, 24*H/10, 2*H);
//        Beam.setPoly(100, 100 + G.rnd(100), 200, 100 + G.rnd(100), 8);
//        g.fillPolygon(Beam.poly);
//        int H = 8, x1 = 100, x2 = 200;
//        Beam.setMasterBeam(x1, 100 + G.rnd(100), x2, 100 + G.rnd(100));
//        g.drawLine(0, Beam.mY1, x1, Beam.mY1);
//        Beam.drawBeamStack(g, 0, 1, x1, x2, H);
//        g.setColor(Color.ORANGE);
//        Beam.drawBeamStack(g, 1, 3, x1 + 10, x2 - 10, H);
//        if (PAGE != null) {
//            Staff staff = PAGE.sysList.get(0).staffs.get(0);
//            Key.drawOnStaff(g, 7, Key.sG, 110, Glyph.SHARP, staff);
//        }
    }

    public void mousePressed(MouseEvent me) {
        curArea.dn(me.getX(), me.getY());
        repaint();
    }
    public void mouseDragged(MouseEvent me) {
        curArea.drag(me.getX(), me.getY());
        repaint();
    }
    public void mouseReleased(MouseEvent me) {
        curArea.up(me.getX(), me.getY());
        trainBtn(me);
        repaint();
    }

    public void trainBtn(MouseEvent me){
        if(me.getX()>(UC.mainWindowWidth-40) && me.getY()<40){
            training = !training; curArea = training ? Shape.TRAINER : Gesture.AREA;
        }
    }

    public void keyTyped(KeyEvent ke) {
        if(training){Shape.TRAINER.keyTyped(ke);repaint();}
    }

    public static void main(String[] args) {
        PANEL = new MusicEd();
        WinApp.launch();
    }
}
