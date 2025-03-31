package reactions;

import graphics.G;
import music.I;
import music.UC;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Set;

/**
 * Class Shape represents a name, e.g. 'A'
 */
public class Shape implements Serializable {

    public static Shape.Database DB = Shape.Database.load();
    public static Shape DOT = DB.get("DOT"); // placeholder for dot shape
    public static Collection<Shape> LIST = DB.values(); // list of key values
    public static Trainer TRAINER = new Trainer();
    public Prototype.List prototypes = new Prototype.List();
    public String name;

    public Shape(String name) {this.name = name;}

    public static Shape recognize(Ink ink) {
        if(ink.vs.size.x < UC.dotThreshold && ink.vs.size.y < UC.dotThreshold) { return DOT; }
        Shape bestMatch = null;
        int bestSoFar = UC.noMatchDist; // assume no match
        for(Shape s:LIST) {
            int d = s.prototypes.bestDist(ink.norm);
            if(d < bestSoFar) {
                bestMatch = s;
                bestSoFar = d;
            }
        }
        return bestMatch;
    }

    // ------------------------- Database -----------------------------------
    public static class Database extends TreeMap<String, Shape> implements Serializable {

        private static String fileName = UC.shapeDatabaseFileName;
        private Database() {
            super(); String dot = "DOT"; put(dot, new Shape(dot));
        }

        private Shape forceGet(String name) {//always return shape
            if(! DB.containsKey(name)) { DB.put(name, new Shape(name)); }
            return DB.get(name);
        }
        public void train(String name, Ink.Norm norm){if(isLegal(name)){forceGet(name).prototypes.train(norm);}}


        // hashmap O(1), treemap O(logn), key value pairs
        public static Database load() {
            Database res;
//            res.put("DOT", new Shape("DOT"));
            try{
                System.out.println("Loading " + fileName);
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
                res = (Shape.Database) ois.readObject();
                System.out.println("Successfully loaded - found" + res.keySet());
//                for(String s : res.keySet()) {//debug - "E-E backspace" ; treats "delete" a char
//                    System.out.print(s);
//                    for(int i = 0; i < s.length(); i++) {
//                        int k = (int)s.charAt(i);//cast char to int
//                        System.out.print("," + k);
//                    }
//                    System.out.println();
//                }
                ois.close();
            } catch(Exception e) {
                System.out.println("Failed to Load.");
                System.out.println(e);
                res = new Database();
            }
            return res;
        }

        public static void save() {
            trimUnusedNames();
            try{
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName));
                oos.writeObject(DB);
                System.out.println("Successfully Saved: " + fileName);
                oos.close();
            } catch(Exception e) {
                System.out.println("Failed database save");
                System.out.println(e);
            }
        }

        public static void trimUnusedNames(){//delete unused data gesture in database like SW-S
            Set<String> strings = DB.keySet();
            ArrayList<String> toRemove = new ArrayList<>();
            for(String key:strings){
                Shape shape = DB.get(key);
                //System.out.println(key + "proto count" + shape.prototypes.size());//debug
                if(shape.prototypes.size() == 0){
                    //System.out.println("removed" + key + "map: " + DB.get(key));//debug
                    toRemove.add(key);
                    //System.out.println("After removed" + key + "map: " + DB.get(key));
                }
            }
            for(String key:toRemove){
                DB.remove(key);
            }
        }

        public boolean isKnown(String name) { return containsKey(name); }
        public boolean unKnown(String name) { return !containsKey(name); }
        public boolean isLegal(String name) { return !name.equals("") && !name.equals("DOT"); }

    }


    // ---------------- Prototype -------------------
    public static class Prototype extends Ink.Norm implements Serializable {
        int nBlend = 1; // keep tracks of how many numbers we had averaged before
        public void blend(Ink.Norm norm) {
            blend(norm, nBlend); nBlend++;
        }
        // ---------------------- Prototype.List -----------------------
        public static class List extends ArrayList<Prototype> implements Serializable {
            public static Prototype bestMatch; // set as a side effect of bestDist
            public int bestDist(Ink.Norm norm) {
                bestMatch = null; //assume fail
                int bestSoFar = UC.noMatchDist; // assume no match
                for (Prototype p : this) {
                    int d = p.dist(norm);
                    if(d < bestSoFar) {
                        bestMatch = p;
                        bestSoFar = d;
                    }
                }
                return bestSoFar;
            }
            private static int m = 10, w = 60, showboxHeight = m + w;
            private static G.VS showbox = new G.VS(m, m, w, w);
            public void show(Graphics g) {
                g.setColor(Color.ORANGE);
                for (int i = 0; i < size(); i++) {
                    Prototype p = get(i);
                    int x = m + i*(m + w);
                    showbox.loc.set(x, m);
                    p.drawAt(g, showbox);
                    g.drawString("" + p.nBlend, x, 20);
                }
            }

            public void train(Ink.Norm norm) {
                if (bestDist(norm) < UC.noMatchDist) { // blend norm
                    bestMatch.blend(norm);
                } else{add(new Shape.Prototype());} // create new proto
            }
        }
    }

    // --------------------------------------TRAINER-----------------------------
    public static class Trainer implements I.Show, I.Area{
        public static String UNKNOWN = " <= this is an unknown shape";
        public static String KNOWN = " <= this is a known shape";
        public static String ILLEGAL = " <= this is NOT a legal Shape name.";

        public static String curName = "";
        public static String curState = ILLEGAL;

        public static Shape.Prototype.List pList = new Shape.Prototype.List();

        public void setState(){
            curState = !Shape.DB.isLegal(curName) ? ILLEGAL : UNKNOWN;
            if (curState == UNKNOWN) {
                if (Shape.DB.isKnown(curName)) {
                    curState = KNOWN;
                    pList = Shape.DB.get(curName).prototypes;
                } else { // it really is UNKNOWN
                    pList = null;
                }
            }
        }

        private boolean removePrototype(int x, int y){//remove bad gesture in database
            int H = Prototype.List.showboxHeight;
            if(y < H){
                int ndx = x/H;
                Prototype.List pList = TRAINER.pList;
                if(pList != null && ndx < pList.size()){pList.remove(ndx);}
                Ink.BUFFER.clear();
                return true;//tell up() we're in showbox area
            }
            return false;
        }

        public void show(Graphics g) {
            G.clearScreen(g);
            g.setColor(Color.BLACK);
            g.drawString(curName, 600, 30);
            g.drawString(curState, 700, 30);
            g.setColor(Color.RED);
            Ink.BUFFER.show(g);
            if (pList != null) { pList.show(g); }
        }

        public boolean hit(int x, int y) {return true;}
        public void dn(int x, int y) {Ink.BUFFER.dn(x, y);}
        public void drag(int x, int y) {Ink.BUFFER.drag(x, y);}
        public void up(int x, int y) {
            if(removePrototype(x, y)){return;}
            Ink.BUFFER.up(x, y);
            Ink ink = new Ink();
            Shape.DB.train(curName, ink.norm);
            setState();
        }

        public void keyTyped(KeyEvent ke) {
            char c = ke.getKeyChar(); System.out.println("typed: " + c);
            curName = (c == ' ' || c == '\n' || c == '\r')? "": curName + c; // \n and \r -> ascii
            if (c == '\n' || c == '\r') { Shape.Database.save(); }
            setState();
        }

    }
}
