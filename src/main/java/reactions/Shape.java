package reactions;

import graphics.G;
import music.UC;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Class Shape represents a name, e.g. 'A'
 */
public class Shape implements Serializable {

    public static Shape.Database DB = Shape.Database.load();
    public static Shape DOT = DB.get("DOT"); // placeholder for dot shape
    public static Collection<Shape> LIST = DB.values(); // list of key values
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

        // hashmap O(1), treemap O(logn), key value pairs
        public static Database load() {
            Database res = new Database();
            res.put("DOT", new Shape("DOT"));
            try{
                System.out.println("Loading " + fileName);
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
                res = (Shape.Database) ois.readObject();
                System.out.println("Successfully loaded - found" + res.keySet());
                ois.close();
            } catch(Exception e) {
                System.out.println("Failed to Load.");
                System.out.println(e);
            }
            return res;
        }

        public static void save() {
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
            private static int m = 10, w = 60;
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
        }
    }
}
