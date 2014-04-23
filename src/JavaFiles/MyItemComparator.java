package JavaFiles;

import jaligner.Sequence;

import java.util.Comparator;

public class MyItemComparator implements Comparator {
    private boolean asc = false;
    private int index =0;
    private int type = 0;
    private int v;  
     
    public MyItemComparator(boolean ascending,int i,int t) {
       asc = ascending;
       index = i;
       type = t;
    }
    public int compare(Object o1, Object o2) {       
        Sequence l1 = (Sequence)o1;
        Sequence l2 = (Sequence)o2;
        double temp1 = (index==3) ? l1.getScore() : (index==4) ? l1.getCoveragePercent() : (index==5) ? l1.getEvalue() : (index==6) ? l1.getIdentities():l1.getCount() ;
        double temp2 = (index==3) ? l2.getScore() : (index==4) ? l2.getCoveragePercent() : (index==5) ? l2.getEvalue() : (index==6) ? l2.getIdentities():l2.getCount() ;        
         
        if(temp1>temp2)
        	v=1;
        else if(temp1<temp2)
        	v=-1;
        else
        	v=0;
//        
//        if (type ==0){//double
//            String s1 = (String)temp1;
//            String s2 = (String)temp2;
//            v = s1.compareTo(s2);  
//        }
//        else{//int
//            Integer s1 = (Integer)temp1;
//            Integer s2 = (Integer)temp2;
//            v = s1.compareTo(s2);
//        } 
         
       return asc ? v: -v;
    }
}