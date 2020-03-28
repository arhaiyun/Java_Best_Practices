package com.exodus.arhaiyun.ByteDance;

/***
 *
 * Write a function to find the longest common prefix string amongst an array of strings.
 *
 *  If there is no common prefix, return an empty string "".
 *
 *  Example 1:
 *
 *  Input: ["flower","flow","flight"]
 *  Output: "fl"
 *  Example 2:
 *
 *  Input: ["dog","racecar","car"]
 *  Output: ""
 *  Explanation: There is no common prefix among the input strings.
 *
 * */

public class LongestCommonPrefix {
    public String longestCommonPrefix(String[] strs) {
        if (strs.length == 0)
            return "";
        String comPreStr = strs[0];
        for (int i = 1; i < strs.length; i++) {
            int j = 0;
            for (; j < comPreStr.length() && j < strs[i].length(); j++) {
                if (comPreStr.charAt(j) != strs[i].charAt(j))
                    break;
            }
            comPreStr = comPreStr.substring(0, j);
            if (comPreStr.equals(""))
                return comPreStr;
        }
        return comPreStr;
    }

    public static void main(String[] args) {
        LongestCommonPrefix solution = new LongestCommonPrefix();
        String[] strs = new String[]{"flower", "flow", "flight"};
        String comPrefix = solution.longestCommonPrefix(strs);
        System.out.printf("LongestCommonPrefix:%s", comPrefix);
    }
}
