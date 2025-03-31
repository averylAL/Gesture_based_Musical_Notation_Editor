package reactions;

import graphics.G;
import music.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class layer do Z-ordering
 */
public class Layer extends ArrayList<I.Show> implements I.Show{
    // initialization error
    // 1. swap twp lines
    // 2. static block
    public static Layer ALL = new Layer("All");
    public static HashMap<String, Layer> byName = new HashMap<>();

    public String name;

    public Layer(String name){
        this.name = name;
        if(!name.equals("All")){
            byName.put(name, this); // byName is every single layer except All
            ALL.add(this);
        }
    }

    public static void nuke() {
        for(I.Show lay : ALL){ // need cast (Layer) lay
            ((Layer) lay).clear();
        }
    }

    public void show(Graphics g){
//        System.out.println("Showing layer: " + name); // debug
        for(I.Show item:this){item.show(g);}
    }

}
