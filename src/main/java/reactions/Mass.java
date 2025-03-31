package reactions;

import music.I;

import java.awt.*;

public abstract class Mass extends Reaction.List implements I.Show{
    public Layer layer;
    /**
     * Constructor
     * @param layerName
     */
    public Mass(String layerName){
        layer = Layer.byName.get(layerName);
        if(layer != null){
            layer.add(this);
        } else{
            System.out.println("Layer " + layerName + " doesn not exist.");
        }
    }
    public void deleteMass(){
        clearAll();
        // need to check equals() before remove(), we want referential equality, compare pointers
        // == ; referential equality (same pointer)
        // value equality (value matches)
        layer.remove(this);
    }
    public void show(Graphics g){} // allow to have Masses that have reactions but show nothing on the screen.
    // fix a bug that shows up when removing masses as I.Shows from layers
    private static int M=1;
    private final int hash = M++; // assign unique hashcode to each Mass
    @Override
    public int hashCode(){return hash;} // each new Mass gets a new M value as a hash code
    @Override
    public boolean equals(Object obj){return this==obj;} // use referential equality
    // end of fix bugs
}
