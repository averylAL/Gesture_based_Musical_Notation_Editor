package music;

import java.util.ArrayList;

public class Time {
    public int x;
    public ArrayList<Head> heads = new ArrayList<>();

    private Time(Sys sys, int x) {
        this.x = x;
        sys.addTime(this);
    }

    public void addHead(Head head){heads.add(head);}
    public void removeHead(Head head){heads.remove(head);}

    public void unStemHeads(int y1, int y2){
        for (Head h : heads){
            int y = h.y();
            if (y > y1 && y < y2){
                h.unStem();
            }
        }
    }

    //---------------------------------List-------------------------------------
    public static class List extends ArrayList<Time> {
        public Sys sys; // lists of times are shared across a single sys
        public List(Sys sys) {this.sys = sys;}
        public Time getTime(int x){
            if(size() == 0) {return new Time(sys, x);}
            Time t = getClosestTime(x);
            return (Math.abs(x - t.x) < UC.snapTime) ? t : new Time(sys, x);
        }
        public Time getClosestTime(int x){
            Time res = get(0);
            int bestSoFar = Math.abs(x - res.x);
            for(Time t: this){
                int dist = Math.abs(x - t.x);
                if(dist < bestSoFar) {res = t; bestSoFar = dist;}
            }
            return res;
        }
    }// end of list

}
