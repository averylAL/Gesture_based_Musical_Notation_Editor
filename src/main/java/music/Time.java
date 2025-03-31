package music;

import java.util.ArrayList;

public class Time {
    public int x;
    public Head.List heads = new Head.List();
    private Time(Sys sys, int x){// factory - apps call getTime(x)
        this.x = x;
        sys.times.add(this);
    }

    public void unStemHeads(int y1, int y2){
        for(Head h: heads){
            int y = h.Y();
            if(y > y1 && y < y2){h.unStem();}
        }
    }

    /*
    public void stemHeads(Staff staff, boolean up, int y1, int y2){
        Stem s = new Stem(staff, up); // first create the new stem
        for(Head h: heads){
            int y = h.Y();
            if(y > y1 && y < y2){ h.joinStem(s); }
        }
        if(s.heads.size() == 0){//debug
            System.out.println("OPS! - empty head list after stemming");
        }else{
            s.setWrongSides();
        }
    }
    */

    //--------------------Time.List------------------------------------
    public static class List extends ArrayList<Time> {
        public Sys sys;
        public List(Sys sys){this.sys = sys;}
        public Time getTime(int x){
            if(size() == 0){return new Time(sys, x);}
            Time t = getClosestTime(x);
            return Math.abs(t.x - x) < UC.snapTime ? t : new Time(sys, x);
        }

        public Time getClosestTime(int x){
            Time res = get(0);
            int bestSoFar = Math.abs(x - res.x);
            for(Time t : this){
                int dist = Math.abs(x - t.x);
                if(dist < bestSoFar){res = t; bestSoFar = dist;}
            }
            return res;
        }
    }
}