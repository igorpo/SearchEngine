package edu.upenn.cis455.testing;


import edu.upenn.cis455.querying.Stemmer;

public class StemTest{

    public static void main(String[] args) {
        String yes;
        Stemmer s;

        yes = "search";
        s = new Stemmer();
        s.add(yes.toCharArray(), yes.length());
        s.stem();
        System.out.println(s.toString());

    }

}
