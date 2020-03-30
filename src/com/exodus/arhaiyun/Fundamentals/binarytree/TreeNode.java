package com.exodus.arhaiyun.Fundamentals.binarytree;

import java.io.IOException;

public class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int x) {
        val = x;
    }

    @Override
    public String toString() {
        return "val: " + val;
    }

}
