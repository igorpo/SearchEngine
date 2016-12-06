package Content;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by igorpogorelskiy on 12/5/16.
 */
public class ContentSeen {

    /**
     * convenience method for telling if two docstrings are similar or not
     * @param a string 1
     * @param b string 2
     * @param threshold threshold to compare to. should be between 0 and 1,
     *                  closer to 1 means more limited (1 is the total equal side) comparison.
     * @return true if similar, else not similar
     */
    public static boolean isSimilar(String a, String b, int threshold) {
        return cosineSimilarity(a, b) >= threshold;
    }

    /**
     * Cosine similarity calculator of the docstrings
     * @param a string 1
     * @param b string 2
     * @return the value of the cosine similarity of the documents.
     *         If it is closer to 1, it is similar, otherwise not really.
     */
    public static double cosineSimilarity(String a, String b) {
        HashMap<String, Integer> fMap = featureMap(a);
        HashMap<String, Integer> sMap = featureMap(b);
        double sum = 0.0;
        double f = normalize(fMap);
        double s = normalize(sMap);

        for (Map.Entry<String, Integer> entry : fMap.entrySet()) {
            String feature = entry.getKey();
            int featureVal = entry.getValue();
            if (sMap.containsKey(feature)) {
                sum += featureVal * sMap.get(feature);
            }
        }
        return sum / (f * s);

    }

    /**
     * Normalizes the feature map. We take the square root
     * of the feature vector components squared sums.
     * @param featureMap map of features
     * @return normalized vector feature values
     */
    public static double normalize(HashMap<String, Integer> featureMap) {
        double n = 0.0;
        for (Map.Entry<String, Integer> entry : featureMap.entrySet()) {
            int featureVal = entry.getValue();
            n += Math.pow(featureVal, 2);
        }
        return Math.sqrt(n);
    }

    /**
     * Get a feature map from the string. The feature map is a basic map of
     * the words in the string to its freq counts
     * @param s string to parse. this should be html docstring
     * @return map of features vector
     */
    public static HashMap<String, Integer> featureMap(String s) {
        HashMap<String, Integer> map = new HashMap<>();
        for (String word : s.toLowerCase().split("\\s+")) {
            if (map.containsKey(word)) {
                int n = map.get(word);
                map.put(word, n + 1);
            } else {
                map.put(word, 1);
            }
        }
        return map;
    }

    /*****************************************
     *               TESTING                 *
     *****************************************/
    public static void main(String[] args) {
        String a = "This is a news article about the french mob.";
        String b = "this is a news article about a french mobster.";
        System.out.println(cosineSimilarity(a, b));
    }
}
