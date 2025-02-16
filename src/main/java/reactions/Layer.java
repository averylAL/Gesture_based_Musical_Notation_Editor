package reactions;

import music.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Layer extends ArrayList<I.Show> implements I.Show {
    public static HashMap<String, Layer> byName = new HashMap<>();
    public static Layer ALL = new Layer("ALL");
    public static void nuke(){
        /*NUKE all layers in preparation for undo.*/
        for(I.Show lay : ALL){((Layer)lay).clear();}//all remains intact, but the layers and thus the masses are cleared
    }
    public String name;
    public Layer(String name){
        this.name = name;
        if(!name.equals("ALL")){
            ALL.add(this);
        }
        byName.put(name, this);
    }

    public static void createAll(String[] a) {for(String s: a){new Layer(s);}}

    @Override
    public void show(Graphics g) {
        for(I.Show item: this){item.show(g);}}

}
