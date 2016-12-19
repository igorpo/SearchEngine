package edu.upenn.cis455.querying;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 package AutoComplete;

 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Set;

 /**
 * Created by igorpogorelskiy on 12/10/16.
 */
public class AutoComplete {

    private static String DICT_PATH = System.getProperty("user.dir") + "/src/dictionary.txt";
    private static HashSet<String> dictionary = new HashSet<>();

    /**
     * Load the dictionary of english words
     */
    public static void loadDictionary() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(DICT_PATH));
            String word;
            while ((word = br.readLine()) != null) {
                dictionary.add(word);
            }
            System.out.println(dictionary.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Suggestions for words within distance r of the source word
     * @param s source word
     * @param r radius of edit distance we care about
     * @return Set of results
     */
    public static Set<String> getSuggestionsWithinRadius(String s, int r) {
        Set<String> results = new HashSet<>();
        for (String word : dictionary) {
            int lDist = levenshtein(s, word);
            if (lDist <= r) {
                results.add(word);
            }
        }
        return results;
    }

    public static Set<String> getSuggestionsWithinTrigramDist(String s, double r) {
        Set<String> results = new HashSet<>();
        for (String word : dictionary) {
            double d = trigram(s, word);
            if (d >= r) {
                results.add(word.toLowerCase());
            }
        }
        return results;
    }

    /**
     * Trigram distance between two strings, more accurate than leven
     * @param s src string
     * @param t tgt string
     * @return the distance as a double
     */
    public static double trigram(String s, String t) {
        String sNew = " " + s.toLowerCase() + " ";
        String tNew = " " + t.toLowerCase() + " ";
        HashSet<String> sTrigrams = populateTrigrams(sNew);
        HashSet<String> tTrigrams = populateTrigrams(tNew);
        sTrigrams.retainAll(tTrigrams);
        int m = sTrigrams.size();
        return ((double ) 2 * m) / (s.length() + t.length());
    }

    private static HashSet<String> populateTrigrams(String sNew) {
        HashSet<String> r = new HashSet<>();
        for (int i = 0; i < sNew.length(); i++) {
            if (i + 2 < sNew.length()) {
                String trigram = sNew.substring(i, i + 3);
                r.add(trigram);
            }
        }
        return r;
    }

    /**
     * Calculate the levenshtein distance between source string
     * and target string
     * @param s source
     * @param t target
     * @return lev. distance
     */
    public static int levenshtein(String s, String t) {
        s = s.toLowerCase();
        t = t.toLowerCase();

        if (s.equals(t)) {
            return 0;
        }

        if (s.isEmpty()) {
            return t.length();
        }

        if (t.isEmpty()) {
            return s.length();
        }

        int[] P = new int[t.length() + 1]; // prev
        int[] T = new int[t.length() + 1];

        // init step
        for (int i = 0; i < P.length; i++) {
            P[i] = i;
        }

        for (int i = 0; i < s.length(); i++) {
            // calc current row indices from prev row
            T[0] = i + 1;
            for (int j = 0; j < t.length(); j++) {
                int c = 1;
                if (s.charAt(i) == t.charAt(j)) {
                    c = 0;
                }
                T[j + 1] = Math.min(T[j] + 1, Math.min(P[j + 1] + 1, P[j] + c));
            }

            // copy current row for prev
            for (int j = 0; j < P.length; j++) {
                P[j] = T[j];
            }
        }

        return T[t.length()];
    }

    public static void main(String[] args) {
        AutoComplete.loadDictionary();
        AutoComplete.getSuggestionsWithinTrigramDist("contrain", 0.52).forEach(System.out::println);
    }
}
