package reactions;

import graphics.*;
import music.UC;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class ShapeTrainer extends WinApp {

//    public static String recognized = "";
//    public static String UNKNOWN = " <= this is an unknown shape";
//    public static String KNOWN = " <= this is a known shape";
//    public static String ILLEGAL = " <= this is NOT a legal Shape name.";
//
//    public static String curName = "";
//    public static String curState = ILLEGAL;
//
//    public static Shape.Prototype.List pList = new Shape.Prototype.List();



    public ShapeTrainer() {
        super("ShapeTrainer", 1000, 800);
    }

//    public void setState(){
////        curState = curName.equals("") || curName.equals("DOT") ? ILLEGAL : UNKNOWN;
//        curState = !Shape.DB.isLegal(curName) ? ILLEGAL : UNKNOWN;
//        if (curState == UNKNOWN) {
//            if (Shape.DB.isKnown(curName)) {
//                curState = KNOWN;
//                pList = Shape.DB.get(curName).prototypes;
//            } else { // it really is UNKNOWN
//                pList = null;
//            }
//        }
//    }

    public void paintComponent(Graphics g) {Shape.TRAINER.show(g);}
//        G.clearScreen(g);
//        g.setColor(Color.BLACK);
//        g.drawString(curName, 600, 30);
//        g.drawString(curState, 700, 30);
//        g.setColor(Color.RED);
//        Ink.BUFFER.show(g);
//        if (pList != null) { pList.show(g); }
//        g.drawString(recognized, 700, 40);


    public void mousePressed(MouseEvent me) { Shape.TRAINER.dn(me.getX(),me.getY()); repaint(); }
    public void mouseDragged(MouseEvent me) { Shape.TRAINER.drag(me.getX(),me.getY()); repaint(); }
    public void mouseReleased(MouseEvent me) { Shape.TRAINER.up(me.getX(), me.getY()); repaint(); }
//        Ink ink = new Ink();
//        Shape.DB.train(curName, ink.norm);
//        setState();
//        repaint();
//        Ink.BUFFER.up(me.getX(),me.getY());
//        if(curState != ILLEGAL) {
//            Ink ink = new Ink();
//            Shape.Prototype proto;
//            if (pList == null) {
//                Shape s = new Shape(curName); // create the shape
//                Shape.DB.put(curName, s);  // add it to the database
//                pList = s.prototypes;      // use its prototype list as the current list
//            }
//            if (pList.bestDist(ink.norm) < UC.noMatchDist) { // we found a match so blend
//                proto = Shape.Prototype.List.bestMatch;
//                proto.blend(ink.norm);
//            } else {
//                proto = new Shape.Prototype();
//                pList.add(proto); // new Prototype
//            }
//            setState();
//            Shape s = Shape.recognize(ink);
//            recognized = "Recognized: " + (s != null ? s.name : "Unrecognized");
//        }
//        repaint();
//    }

    public void keyTyped(KeyEvent ke) {
//        char c = ke.getKeyChar(); System.out.println("typed: " + c);
//        curName = (c == ' ' || c == '\n' || c == '\r')? "": curName + c; // \n and \r -> ascii
//        if (c == '\n' || c == '\r') { Shape.Database.save(); }
//        setState();
        Shape.TRAINER.keyTyped(ke);
        repaint();
    }

    public static void main(String[] args) {
        PANEL = new ShapeTrainer();
        WinApp.launch();
    }
}
