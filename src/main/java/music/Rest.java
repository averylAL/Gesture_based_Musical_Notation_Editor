package music;

import reactions.Gesture;
import reactions.Reaction;

import java.awt.*;

public class Rest extends Duration{
    public static Glyph[] glyphs = {
       Glyph.REST_W, Glyph.REST_H, Glyph.REST_Q, Glyph.REST_1F, Glyph.REST_2F, Glyph.REST_3F, Glyph.REST_4F
    };
    public Staff staff;
    public Time time;
    public int line = 4;//default location of any rest, most rest print on center line

    public Rest(Staff staff, Time time){
        this.staff = staff;
        this.time = time;

        addReaction(new Reaction("E-E") {//add Flag to Rest
            @Override
            public int bid(Gesture g) {
                int y = g.vs.yM(), x1 = g.vs.xL(), x2 = g.vs.xH(), x = Rest.this.time.x;
                if((x1 > x) || (x2 < x)){return UC.noBid;}
                if(y < staff.yTop() || y > staff.yBot()){return UC.noBid;}
                return Math.abs(y - Rest.this.staff.yLine(4));
            }
            @Override
            public void act(Gesture g) {Rest.this.incFlag();}
        });

        addReaction(new Reaction("W-W") {//add decFlag to Rest
            @Override
            public int bid(Gesture g) {
                int y = g.vs.yM(), x1 = g.vs.xL(), x2 = g.vs.xH(), x = Rest.this.time.x;
                if((x1 > x) || (x2 < x)){return UC.noBid;}
                if(y < staff.yTop() || y > staff.yBot()){return UC.noBid;}
                return Math.abs(y - Rest.this.staff.yLine(4));
            }
            @Override
            public void act(Gesture g) {Rest.this.decFlag();}
        });

        addReaction(new Reaction("DOT") {//Dotting the Rest
            @Override
            public int bid(Gesture g) {
                int xR = Rest.this.time.x, yR = Rest.this.y();
                int x = g.vs.xM(), y = g.vs.yM();
                if((x < xR) || (x > xR + 40) || (y < yR -40) || (y > yR + 40)){return UC.noBid;}
                return Math.abs(x - xR) + Math.abs(y - yR);
            }
            @Override
            public void act(Gesture g) {Rest.this.cycleDot();}
        });
    }

    public int y(){return staff.yLine(line);}

    public void show(Graphics g){
        int H = staff.fmt.H, y = y();
        Glyph glyph = glyphs[nFlag + 2];//nFlag can get down to -2
        glyph.showAt(g, H, time.x, y);
        int off = UC.AugDotOffSet, sp = UC.AugDotSpacing;
        for (int i = 0; i < nDot; i++){
            g.fillOval(time.x + off + i * sp, y - 3*H/2, H*2/3, H*2/3);
        }
    }
}
