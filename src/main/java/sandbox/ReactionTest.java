package sandbox;

import graphics.*;
import music.UC;
import reactions.*;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ReactionTest extends WinApp {
    static {
        new Layer("BACK");
        new Layer("FORE");
    }
    public ReactionTest() {
        super("ReactionTest", 1000, 800);
        Reaction.initialReactions.addReaction(new Reaction("SW-SW") {
            @Override
            public int bid(Gesture g){
//                System.out.println("Bid called"); // bid debug
                return 0;
            }
            @Override
            public void act(Gesture g) {
                new Box(g.vs);
//                System.out.println("Act called");// act debug
            }
        });
    }
    @Override
    public void paintComponent(Graphics g) {
        G.clearScreen(g);
        Layer.ALL.show(g);
        g.setColor(Color.BLUE);
        Ink.BUFFER.show(g); // show ink on top of drawn graphics
        g.drawString(Gesture.recognized, 900, 30); // gesture debug info
    }

    public void mousePressed(MouseEvent me){Gesture.AREA.dn(me.getX(),me.getY()); repaint();}
    public void mouseDragged(MouseEvent me){Gesture.AREA.drag(me.getX(),me.getY()); repaint();}
    public void mouseReleased(MouseEvent me){Gesture.AREA.up(me.getX(),me.getY()); repaint();}

    public static void main(String[] args) {
        PANEL = new ReactionTest();
        WinApp.launch();
    }
    public static class Box extends Mass {
        public G.VS vs;
        public Color c = G.rndColor();
        public Box(G.VS vs) {
            super("BACK");
            this.vs = vs;

            addReaction(new Reaction("S-S") {//delete a box
                @Override
                public int bid(Gesture g) {
                    int x = g.vs.xM(); // middle value
                    int y = g.vs.yL();
                    // can also write if(vs.hit(x, y)), take out the Box.this is fine
                    if(Box.this.vs.hit(x, y)) {//if hit, return how good it matches
                        return Math.abs(x - Box.this.vs.xM());
                    } else{return UC.noBid;}
                }
                @Override
                public void act(Gesture g) {
                    Box.this.deleteMass();
                }
            });
        }
        @Override
        public void show(Graphics g) {
            vs.fill(g, c); // fill box with color
        }
    }
}
