package cn.edu.fudan.se.util;

import org.apache.commons.text.similarity.CosineSimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 相似度比较
 */
public class SimilarityUtils {

    static CosineSimilarity cosineSimilarity = new CosineSimilarity();

    public static double compareCosineSimilarity(String s1, String s2) {
        Map<CharSequence, Integer> s1Map = generateStringMap(s1);
        Map<CharSequence, Integer> s2Map = generateStringMap(s2);
        Double level = cosineSimilarity.cosineSimilarity(s1Map, s2Map);
        return level;
    }

    public static String longestCommonSubSequence(String a, String b) {
        String x;
        String y;
        int aLength = a.length();
        int bLength = b.length();
        if (aLength == 0 || bLength == 0) {
            return "";
        } else if (a.charAt(aLength - 1) == b.charAt(bLength - 1)) {
            return longestCommonSubSequence(a.substring(0, aLength - 1), b.substring(0, bLength - 1)) + a.charAt(aLength - 1);
        } else {
            x = longestCommonSubSequence(a, b.substring(0, bLength - 1));
            y = longestCommonSubSequence(a.substring(0, aLength - 1), b);
        }
        return (x.length() > y.length()) ? x : y;
    }

    public static int findLCS(String A, int n, String B, int m) {
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= m; j++) {
                dp[i][j] = 0;
            }
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                if (A.charAt(i - 1) == B.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = dp[i - 1][j] > dp[i][j - 1] ? dp[i - 1][j] : dp[i][j - 1];
                }
            }
        }
        return dp[n][m];

    }

    /**
     * 比较token的相似度
     *
     * @param name1
     * @param name2
     * @return
     */
    public static double tokenSimilarity(String name1, String name2) {
        List<String> token1List = splitToken(name1);
        List<String> token2List = splitToken(name2);

        return tokenSimilarity(token1List, token2List);
    }

    /**
     * 比较
     *
     * @param token1List
     * @param token2List
     * @return
     */
    public static double tokenSimilarity(List<String> token1List, List<String> token2List) {

        int size1 = token1List.size();
        int size2 = token2List.size();

        if (size1 == 0 && size2 == 0) {
            return 1;
        }
        if (size1 == 0 || size2 == 0) {
            return 0;
        }

        int sameCount = 0;

        for (int index = 0; index < size1; index++) {
            String token1 = token1List.get(index);

            int index2 = token2List.indexOf(token1);
            if (index2 != -1) {
                token2List.remove(index2);
                sameCount++;
            }
        }
        return (sameCount * 1.0 / size1 + sameCount * 1.0 / size2) / 2;
    }

    public static List<String> splitToken(String name) {
/*
        if (name.contains("$")) {
            String[] names = name.split("\\$");

            name = names[names.length - 1];
        }
*/

        List<String> tokenList = new ArrayList<>();

        int startIndex = 0;

        int size = name.length();

        for (int i = 0; i < size; i++) {

            char c = name.charAt(i);

            if (c - 'A' >= 0 && c - 'Z' <= 0) {
                //找到驼峰命名法的大写字母
                String token = name.substring(startIndex, i);
                if (token.length() != 0) {
                    tokenList.add(token.toLowerCase());
                    startIndex = i;
                }
            }
        }

        tokenList.add(name.substring(startIndex).toLowerCase());
        return tokenList;
    }


    /**
     * 基于最长公共子串的相似度
     *
     * @param a
     * @param b
     * @return
     */
    public static double lcsSimilarity(String a, String b) {
        if (a.length() == 0 || b.length() == 0) {
            return 0;
        }

        //String lcs = longestCommonSubSequence(a, b);

        int lcsLength = findLCS(a, a.length(), b, b.length());

        return (lcsLength * 1.0 / a.length() + lcsLength * 1.0 / b.length()) / 2;

        // return (lcs.length() * 1.0 / a.length() + lcs.length() * 1.0 / b.length()) / 2;

    }


    private static Map<CharSequence, Integer> generateStringMap(String s) {

        Map<CharSequence, Integer> maps = new HashMap<>();

        String[] sSplits = s.trim().split(" ");
        for (String sItem : sSplits) {
            if (maps.containsKey(sItem)) {
                maps.put(sItem, maps.get(sItem) + 1);
            } else {
                maps.put(sItem, 1);
            }
        }

        return maps;
    }

}
