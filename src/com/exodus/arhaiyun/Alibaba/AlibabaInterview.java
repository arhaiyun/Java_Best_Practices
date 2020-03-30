package com.exodus.arhaiyun.Alibaba;

import com.exodus.arhaiyun.Fundamentals.binarytree.DealFileText;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AlibabaInterview {
    public static void aliArticleWordCounter(String fileName) throws IOException {
        File file = new File(fileName);
        DealFileText dft = new DealFileText(file, 4, 1024 * 1024 * 10);
        dft.doFile();
    }

    public static boolean wordPatternMatcher(String pattern, String str) {
        //Use charStringMap to  store the pattern character and the first match string
        Map<Character, String> charStringMap = new HashMap<>();
        char[] patterCharArr = pattern.toCharArray();
        String[] strArr = str.split(" ");
        if (patterCharArr.length != strArr.length) {
            return false;
        }
        for (int i = 0; i < patterCharArr.length; i++) {
            String val = charStringMap.get(patterCharArr[i]);
            if (null != val) {
                return val.equals(strArr[i]);
            } else {
                if (!charStringMap.values().contains(strArr[i])) {
                    charStringMap.put(patterCharArr[i], strArr[i]);
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        aliArticleWordCounter("D:\\alitest.txt");
        System.out.println(wordPatternMatcher("abba", "北京 杭州 杭州 北京"));
        System.out.println(wordPatternMatcher("aabb", "北京 杭州 杭州 北京"));
        System.out.println(wordPatternMatcher("baab", "北京 杭州 杭州 北京"));
    }
}
