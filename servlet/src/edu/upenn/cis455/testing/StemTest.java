package edu.upenn.cis455.testing;


import edu.upenn.cis455.querying.Stemmer;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class StemTest{

    public static void main(String[] args) {

        int n = 3;
        List<Integer> l = new LinkedList<>();
        l.add(1);
        l.add(4);
        l.add(35);
        l.add(1000);
        l.add(3);
        l.add(100);


        PriorityQueue<Integer> pq = new PriorityQueue<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer i1, Integer i2) {
                double val1 = i1;
                double val2 = i2;
                if (val1 > val2){
                    return 1;
                } else if (val1 < val2){
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        for (Integer i : l){
            pq.offer(i);
            if (pq.size() > n){
                pq.poll();
            }
        }

        for (Integer i : pq){
            System.out.println("value: " + i );
        }
        Stemmer s = new Stemmer();
        s.add("donald".toCharArray(), "donald".length());
        s.stem();
        System.out.println(s.toString());
        s = new Stemmer();
        s.add("ives".toCharArray(), "ives".length());
        s.stem();
        System.out.println(s.toString());

    }

}
