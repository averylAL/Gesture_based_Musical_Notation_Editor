package reactions;

import graphics.*;
import music.*;

import java.util.ArrayList;

/**
 * Class Gesture keep track of WHAT was done on the screen
 * E-E: flag
 * SW-SW: note
 * vertical line: stem
 * top->bottom + dot: repeat measure bar
 * top->bottom: measure bar
 */
public class Gesture {
    private static List UNDO = new List();
    public Shape shape;
    public G.VS vs;

    /**
     *  Private constructor
     *  Private const means two ways:singleton; factor method
     */
    private Gesture(Shape shape, G.VS vs) {
        this.shape = shape;
        this.vs = vs;
    }
    public static Gesture getNew(Ink ink) { // can return null
        Shape s = Shape.recognize(ink);
        return s == null ? null : new Gesture(s, ink.vs);
    }

    private void redoGesture(){
        Reaction r = Reaction.best(this);
        if(r != null){ r.act(this);}
    }
    private void doGesture(){ // if the gesture causes a reaction, then add to undo list and do it.
        Reaction r = Reaction.best(this);
        if(r != null){ UNDO.add(this); r.act(this);} else {recognized += " no bids";}
    }
    public static void undo(){
        if(!UNDO.isEmpty()){
            UNDO.remove(UNDO.size()-1); // remove last element
            Layer.nuke();  // eliminates all the masses
            Reaction.nuke();  // clears byShape then reloads initial reactions
            UNDO.redo();
        }
    }

    /**
     * Anonymous Class
     */
    public static I.Area AREA = new I.Area() {
        @Override
        public boolean hit(int x, int y) {return true;}
        @Override
        public void dn(int x, int y) {Ink.BUFFER.dn(x, y);}
        @Override
        public void drag(int x, int y) {Ink.BUFFER.drag(x, y);}
        @Override
        public void up(int x, int y) {
            Ink.BUFFER.up(x, y);
            Ink ink = new Ink();
            Gesture gest = Gesture.getNew(ink);
            Ink.BUFFER.clear();
            recognized = gest == null ? "null" : gest.shape.name;
            if (gest != null) {
                if (gest.shape.name.equals("N-N")) {// hardwired Undo
                    undo();
                } else{ gest.doGesture();} //replace all the code below
//                Reaction r = Reaction.best(gest); // best reaction finding the gesture
//                if (r != null) {
//                    r.act(gest);
//                } else{ recognized += " no bids"; }
            }
        }
    }; // end of AREA
    public static String recognized = "null"; // instrumented tracking when gestures don't work

    //-------------------------------------LIST----------------------------------\
    public static class List extends ArrayList<Gesture> {
        private void redo(){
            for(Gesture gest : this) {
                gest.redoGesture();
            }
        }
    }

}
