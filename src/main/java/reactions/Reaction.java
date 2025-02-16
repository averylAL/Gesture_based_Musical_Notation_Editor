package reactions;

import music.I;
import graphics.*;
import music.UC;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Reaction implements I.React {
    public static I.Act initialAct;
    private static Map byShape = new Map();
    public static List initialReactions = new List();
    public Shape shape;

    public Reaction(String shapeName){
        shape = Shape.DB.get(shapeName);
        if(shape == null){System.out.println("Ops! - Shape.DB don't know about: "+shapeName);}
    }

    public void enable() {
        List list = byShape.getList(shape);
        if(!list.contains(this)){
            list.add(this);
        }
    }
    public void disable() {
        List list = byShape.getList(shape);
        list.remove(this);
    }
    public static Reaction best(Gesture g) {
        return byShape.getList(g.shape).loBid(g);
    }
    public static void nuke() {
        byShape.clear();
        initialReactions.enable();
    }

    //--------------------- LIST ----------------------
    public static class List extends ArrayList<Reaction> {
        /**
         * adding and removing is done to TWO lists,
         * the one in a Mass and the one in the byShape Map
         */
        public void addReaction(Reaction r) { add(r); r.enable(); }
        public void enable(){for (Reaction r : this){r.enable();}}
        public void removeReaction(Reaction r) { remove(r); r.disable();}

        public void clearAll() {
            /* to avoid concurrent array mods,
            first remove all from shape map, then clear*/
            for(Reaction r : this) {r.disable();}
            this.clear();
        }

        public Reaction loBid(Gesture g){ // can return null - list is Empty or no one wants to bid.
            Reaction res = null; int bestSoFar = UC.noBid;
            for(Reaction r : this) {
                int b = r.bid(g);
                if(b < bestSoFar) {
                    bestSoFar = b;
                    res = r;
                }
            }
            return res;
        }
    }// end of LIST
    //--------------------- Map -----------------------
    public static class Map extends HashMap<Shape, List> {
        public List getList(Shape s){ // always succeeds
            List res = get(s);
            if(res == null) { res = new List(); put(s,res); }
            return res;
        }
    }// end of Map
}
