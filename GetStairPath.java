import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
         Scanner scn=new Scanner (System.in);
         int n=scn.nextInt();
         ArrayList<String> fr = getStairPaths(n);
         System.out.print(fr);
    }

    public static ArrayList<String> getStairPaths(int n) {
        if(n==0){
             ArrayList<String> bas=new ArrayList<>();
             bas.add("");
             return bas;
            
            
        }
        else if(n<0){
             ArrayList<String> bas=new ArrayList<>();
             return bas;
            
        }
        
        ArrayList<String> p1 = getStairPaths(n-1);
        ArrayList<String> p2 = getStairPaths(n-2);
        ArrayList<String> p3 = getStairPaths(n-3);
        ArrayList<String> rej=new ArrayList<>();
        
        for(String val:p1){
            rej.add(1 + val);
        }
         for(String val:p2){
            rej.add(2 + val);
        }
         for(String val:p3){
            rej.add(3 + val);
        }
        return rej;
        
        
        
    }

}
