package reactions;

import music.I;

import java.awt.*;

/**
 * A Mass is the type of object that lives in a layer
 */
public abstract class Mass extends Reaction.List implements I.Show{
    public Layer layer;
    public Mass(String layerName){
        this.layer = Layer.byName.get(layerName);
        if(layer!=null) {
            layer.add(this);
        } else {
            System.out.println("BAD LAYER NAME-" + layerName);
        }
    }
    public void deleteMass(){
        clearAll(); // clears all reactions from this list AND from the Reaction byShape Map
        layer.remove(this); // remove self from layers.
    }

    // fix a bug that shows up when removing masses as I.Shows from layers
    private static int M=1;
    private int hash = M++; // assign unique hash code to each Mass
    @Override
    public int hashCode(){return hash;} // each new Mass gets a new M value as a hash code
    @Override
    public boolean equals(Object o){return this==o;} // use referential equality
    public void show(Graphics g){}
}
