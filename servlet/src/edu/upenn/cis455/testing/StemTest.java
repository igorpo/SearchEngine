package edu.upenn.cis455.testing;


import edu.upenn.cis455.querying.Stemmer;

public class StemTest{

    public static void main(String[] args) {

        String w = args[0];
        Stemmer stemmer = new Stemmer();

        stemmer.add(w.toCharArray(), w.length());
        stemmer.stem();
        String stemmedWord = stemmer.toString();
        System.out.println("WORD:" + w);
        System.out.println("STEMMED WORD:" + stemmedWord);
    }

}
