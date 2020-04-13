package com.exodus.arhaiyun.bytedance;

import java.util.Deque;
import java.util.LinkedList;

public class SimplifyPath {
    public String simplifyPath(String path) {
        Deque<String> stack = new LinkedList<>();
        for (String item : path.split("/")) {
            if (item.equals("..")) {
                if (!stack.isEmpty()) stack.pop();
            } else if (!item.isEmpty() && !item.equals(".")) {
                stack.push(item);
            }
        }
        StringBuilder res = new StringBuilder();
        for (String d : stack) {
            res.insert(0, "/" + d);
        }
        return (res.length() == 0) ? "/" : res.toString();
    }

    public static void main(String[] args) {
        SimplifyPath instance = new SimplifyPath();
        String path = "/a//b////c/d//././/..";
        System.out.println(instance.simplifyPath(path));
    }
}
