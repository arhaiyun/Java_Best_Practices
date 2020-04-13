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
        String res = "";
        for (String d : stack) res = "/" + d + res;
        return res.isEmpty() ? "/" : res;
    }

    public static void main(String[] args) {
        SimplifyPath instance = new SimplifyPath();
        String path = "/a//b////c/d//././/..";
        System.out.println(instance.simplifyPath(path));
    }
}
