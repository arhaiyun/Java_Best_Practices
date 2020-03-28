package com.exodus.arhaiyun.ByteDance;

/***
 *
 * Given an input string, reverse the string word by word.
 *
 *
 * Example 1:
 *
 * Input: "the sky is blue"
 * Output: "blue is sky the"
 * Example 2:
 *
 * Input: "  hello world!  "
 * Output: "world! hello"
 * Explanation: Your reversed string should not contain leading or trailing spaces.
 * Example 3:
 *
 * Input: "a good   example"
 * Output: "example good a"
 * Explanation: You need to reduce multiple spaces between two words to a single space in the reversed string.
 *  
 *
 * Note:
 *
 * A word is defined as a sequence of non-space characters.
 * Input string may contain leading or trailing spaces. However, your reversed string should not contain leading or trailing spaces.
 * You need to reduce multiple spaces between two words to a single space in the reversed string.
 *
 * 来源：力扣（LeetCode）
 * 链接：https://leetcode-cn.com/problems/reverse-words-in-a-string
 * 著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。
 *
 * */

public class ReverseWords {
    public String reverseWords(String s) {
        if (s == null) {
            return null;
        }
        String[] ss = s.split(" ");
        int n = ss.length;
        StringBuilder ans = new StringBuilder();
        for (int i = n - 1; i >= 0; i--) {
            if (ss[i].length() == 0) continue;
            ans.append(ss[i]).append(" ");
        }
        if (ans.length() == 0) {
            return "";
        }
        ans.deleteCharAt(ans.length() - 1);
        return ans.toString();
    }

    public static void main(String[] args) {
        ReverseWords solution = new ReverseWords();
        String s = "a good   example";
        System.out.printf("Reverse words:%s", solution.reverseWords(s));
    }
}
