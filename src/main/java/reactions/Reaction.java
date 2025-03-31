package reactions;

import music.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Base class
 * all base class: reaction, layer, mass, gesture
 */
public abstract class Reaction implements I.React{
    private static Map byShape = new Map();
    public static List initialReactions = new List();
    public Shape shape;

    public Reaction(String shapeName){
        shape = Shape.DB.get(shapeName);
        if(shape == null){System.out.println("OPS? Shape Database does NOT have: " + shapeName);}
    }

    public void enable(){List list = byShape.getList(shape); if(!list.contains(this)){list.add(this);}} // check if already in line
    public void disable(){List list = byShape.getList(shape); list.remove(this);}

    public static Reaction best(Gesture g){return byShape.getList(g.shape).loBid(g);} // can fail, can return null; best reaction

    public static void nuke(){ // used to reset for UNDO
        byShape = new Map(); // throw away all the reactions.
        initialReactions.enable(); // ensures that the initial reaction is still in the byShape Map
    }

    //---------------------------------------LIST--------------------------------------
    public static class List extends ArrayList<Reaction> {
        public void addReaction(Reaction r){add(r); r.enable();}
        public void removeReaction(Reaction r){remove(r); r.disable();}

        public void enable(){for(Reaction r : this){r.enable();}} // enables entire list

        public void clearAll(){for(Reaction r: this){r.disable();} this.clear();} // first disable from map, then clear

        public Reaction loBid(Gesture g){
            Reaction res = null; // can fail
            int bestSoFar = UC.noBid;
            for(Reaction r: this){
                int b=r.bid(g);
                if(b<bestSoFar){bestSoFar=b; res=r;}
            }
            return res;
        }
    }
    //---------------------------------------MAP---------------------------------------
    public static class Map extends HashMap<Shape, List> {
        public List getList(Shape s){// always succeed
            List res = get(s);
            if(res == null){res = new List(); put(s, res);}
            return res;
        }
    }

}
