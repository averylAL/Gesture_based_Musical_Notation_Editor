package reactions;

import graphics.*;
import music.I;

import java.util.ArrayList;

public class Gesture {
    public static List UNDO = new List();
    public static String recognized = "null"; //instrumented tracking when gestures don't work
    public Shape shape;
    public G.VS vs;

    private Gesture(Shape shape, G.VS vs) {
        this.shape = shape;
        this.vs = vs;
    }

    public static Gesture getNew(Ink ink) { // can return null
        Shape s = Shape.recognize(ink);
        return (s==null) ? null : new Gesture(s,ink.vs);
    }
    public void doGesture(){//if the gesture causes a reaction, then add to undo list and do it.
        Reaction r = Reaction.best(this);
        if(r != null) {
            UNDO.add(this);r.act(this);
        } else {
            recognized += " no bids";
        }
    }
    public void redoGesture(){
        Reaction r = Reaction.best(this);
        if(r != null) {r.act(this);}
    }
    public static void undo(){
        if(UNDO.size() == 0){return;}
        UNDO.remove(UNDO.size() - 1);//remove last element
        Layer.nuke(); //eliminates all the masses
        Reaction.nuke();//clears byShape then reloads initial reactions
        UNDO.redo();
    }

    public static I.Area AREA = new I.Area(){
        public boolean hit(int x, int y) { return true; }
        public void dn(int x, int y) {Ink.BUFFER.dn(x,y);}
        public void drag(int x, int y) {Ink.BUFFER.drag(x,y);}
        public void up(int x, int y){
            Ink.BUFFER.add(x,y);
            Ink ink = new Ink();
            Gesture gest = Gesture.getNew(ink); // can fail if unrecognized
            Ink.BUFFER.clear();
            recognized = (gest == null)?"null": gest.shape.name; // debug info
            if(gest != null){
                if(gest.shape.name.equals("N-N")) {
                    undo();
                }else {
                    gest.doGesture();
                }
            }

            /*if(gest != null){
                Reaction r = Reaction.best(gest); // this can also fail - possibly no reaction wants it.
                if(r != null){r.act(gest);}
            }*/
        }
    };
    //-------------------------------List------------------------------
    public static class List extends ArrayList<Gesture>{
        private void redo(){for (Gesture g : this) {g.redoGesture();}}
    }
}
