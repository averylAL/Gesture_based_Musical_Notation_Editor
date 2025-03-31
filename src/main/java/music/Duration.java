package music;

import reactions.*;

import java.awt.*;

public abstract class Duration extends Mass {
    public int nFlag = 0, nDot = 0;//default is 0, no flag, no dots
    public Duration() {super("NOTE");}
    public abstract void show(Graphics g);

    public void incFlag(){if (nFlag < 4) nFlag++;}
    public void decFlag(){if (nFlag > -2) nFlag--;}
    public void cycleDot(){nDot++; if(nDot > 3) {nDot = 0;}} //null, 1, 2, 3 - cycle round
}
