package AutoComplete;

/**
 * Created by igorpogorelskiy on 12/10/16.
 */
public class AutoComplete {

    
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

        int[] P = new int[s.length() + 1]; // prev
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
                if (t.charAt(i) == t.charAt(j)) {
                    c = 0;
                }
                T[j] = Math.min(T[j] + 1, Math.min(P[j + 1] + 1, P[j] + c));
            }

            // copy current row for prev
            for (int j = 0; j < P.length; j++) {
                P[j] = T[j];
            }
        }

        return T[t.length()];
    }

    public static void main(String[] args) {
        String s = "kitten";
        String t = "kiten";
        System.out.println(levenshtein(s, t));
    }
}
