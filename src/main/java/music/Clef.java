package music;

import reactions.Gesture;
import reactions.Mass;
import reactions.Reaction;

import java.awt.*;
import java.util.ArrayList;

public class Clef extends Mass implements Comparable<Clef> {

    public static final Glyph G = Glyph.CLEF_G, F = Glyph.CLEF_F;

    public Glyph glyph;
    public Staff staff;
    public int x;

    public Clef(Staff staff, int x, Glyph glyph) {
        super("NOTE");
        this.staff = staff;
        this.x = x;
        this.glyph = glyph;

        addReaction(new Reaction("DOT") {
            @Override
            public int bid(Gesture g) {
                return Math.abs(g.vs.xM() - Clef.this.x) + Math.abs(g.vs.yM() - Clef.this.staff.yLine(4));
            }

            @Override
            public void act(Gesture g) {
                toggleClef();
            }
        });


    }

    public void toggleClef(){glyph = glyph == G? F: G;}

    @Override
    public void show(Graphics g){glyph.showAt(g,staff.fmt.H,x,staff.lineOfY(4));}

    @Override
    public int compareTo(Clef c){return x-c.x;}

    //--------------------List---------------
    public static class List extends ArrayList<Clef> {}
}
