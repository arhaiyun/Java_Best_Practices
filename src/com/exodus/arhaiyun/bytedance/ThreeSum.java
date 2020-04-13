package com.exodus.arhaiyun.bytedance;

/**
 *
 * Given an array nums of n integers, are there elements a, b, c in nums such that a + b + c = 0? Find all unique triplets in the array which gives the sum of zero.
 *
 * Note:
 *
 * The solution set must not contain duplicate triplets.
 *
 * Example:
 *
 * Given array nums = [-1, 0, 1, 2, -1, -4],
 *
 * A solution set is:
 * [
 *   [-1, 0, 1],
 *   [-1, -1, 2]
 * ]
 *
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreeSum {
    public static List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> ans = new ArrayList();
        int len = nums.length;
        if (len < 3) return ans;
        Arrays.sort(nums); // 排序
        for (int i = 0; i < len; i++) {
            if (nums[i] > 0)
                break; // 如果当前数字大于0，则三数之和一定大于0，所以结束循环
            if (i > 0 && nums[i] == nums[i - 1])
                continue; // 去重
            int L = i + 1;
            int R = len - 1;
            while (L < R) {
                int sum = nums[i] + nums[L] + nums[R];
                if (sum == 0) {
                    ans.add(Arrays.asList(nums[i], nums[L], nums[R]));
                    while (L < R && nums[L] == nums[L + 1]) L++; // 去重
                    while (L < R && nums[R] == nums[R - 1]) R--; // 去重
                    L++;
                    R--;
                } else if (sum < 0) {
                    L++;
                } else {
                    R--;
                }
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        ThreeSum instance = new ThreeSum();
        int[] num = new int[]{-1, 0, 1, 2, -1, -4};
        System.out.println(instance.threeSum(num));
    }
}
