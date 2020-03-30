package com.exodus.arhaiyun.Fundamentals.binarytree;

import java.util.*;

public class BTreeAlgos {
    public void visit(TreeNode node) {
        System.out.print(node.val + " ");
    }

    /**
     * 递归先序遍历
     */
    public void preOrderRecursion(TreeNode node) {
        if (node == null) //如果结点为空则返回
            return;
        visit(node);//访问根节点
        preOrderRecursion(node.left);//访问左孩子
        preOrderRecursion(node.right);//访问右孩子
    }

    /**
     * 非递归先序遍历二叉树
     */
    public List<Integer> preorderTraversal(TreeNode root) {
        List<Integer> resultList = new ArrayList<>();
        Stack<TreeNode> treeStack = new Stack<>();
        if (root == null) //如果为空树则返回
            return resultList;
        treeStack.push(root);
        while (!treeStack.isEmpty()) {
            TreeNode tempNode = treeStack.pop();
            if (tempNode != null) {
                resultList.add(tempNode.val);//访问根节点
                treeStack.push(tempNode.right); //入栈右孩子
                treeStack.push(tempNode.left);//入栈左孩子
            }
        }
        return resultList;
    }

    /**
     * 递归中序遍历
     */
    public void inorderRecursion(TreeNode node) {
        if (node == null) //如果结点为空则返回
            return;
        inorderRecursion(node.left);//访问左孩子
        visit(node);//访问根节点
        inorderRecursion(node.right);//访问右孩子
    }

    /**
     * 非递归中序遍历
     */
    public List<Integer> inorderTraversalNonCur(TreeNode root) {
        List<Integer> visitedList = new ArrayList<>();
        Map<TreeNode, Integer> visitedNodeMap = new HashMap<>();//保存已访问的节点
        Stack<TreeNode> toBeVisitedNodes = new Stack<>();//待访问的节点
        if (root == null)
            return visitedList;
        toBeVisitedNodes.push(root);
        while (!toBeVisitedNodes.isEmpty()) {
            TreeNode tempNode = toBeVisitedNodes.peek(); //注意这里是peek而不是pop
            while (tempNode.left != null) { //如果该节点的左节点还未被访问，则需先访问其左节点
                if (visitedNodeMap.get(tempNode.left) != null) //该节点已经被访问（不存在某个节点已被访问但其左节点还未被访问的情况）
                    break;
                toBeVisitedNodes.push(tempNode.left);
                tempNode = tempNode.left;
            }
            tempNode = toBeVisitedNodes.pop();//访问节点
            visitedList.add(tempNode.val);
            visitedNodeMap.put(tempNode, 1);//将节点加入已访问map
            if (tempNode.right != null) //将右结点入栈
                toBeVisitedNodes.push(tempNode.right);
        }
        return visitedList;
    }

    public List<Integer> inorderTraversal(TreeNode root) {
        List<Integer> list = new ArrayList<Integer>();

        Stack<TreeNode> stack = new Stack<TreeNode>();
        TreeNode cur = root;

        while (cur != null || !stack.empty()) {
            while (cur != null) {
                stack.add(cur);
                cur = cur.left;
            }
            cur = stack.pop();
            list.add(cur.val);
            cur = cur.right;
        }

        return list;
    }

    /**
     * 非递归后序遍历
     */
    public List<Integer> postOrderNonCur(TreeNode root) {
        List<Integer> resultList = new ArrayList<>();
        if (root == null)
            return resultList;
        Map<TreeNode, Integer> visitedMap = new HashMap<>();
        Stack<TreeNode> toBeVisitedStack = new Stack<>();
        toBeVisitedStack.push(root);
        while (!toBeVisitedStack.isEmpty()) {
            TreeNode tempNode = toBeVisitedStack.peek(); //注意这里是peek而不是pop
            if (tempNode.left == null && tempNode.right == null) { //如果没有左右孩子则访问
                resultList.add(tempNode.val);
                visitedMap.put(tempNode, 1);
                toBeVisitedStack.pop();
                continue;
            } else if (!((tempNode.left != null && visitedMap.get(tempNode.left) == null) || (tempNode.right != null && visitedMap.get(tempNode.right) == null))) {
                //如果节点的左右孩子均已被访问
                resultList.add(tempNode.val);
                toBeVisitedStack.pop();
                visitedMap.put(tempNode, 1);
                continue;
            }
            if (tempNode.left != null) {
                while (tempNode.left != null && visitedMap.get(tempNode.left) == null) {//左孩子没有被访问
                    toBeVisitedStack.push(tempNode.left);
                    tempNode = tempNode.left;
                }
            }
            if (tempNode.right != null) {
                if (visitedMap.get(tempNode.right) == null) {//右孩子没有被访问
                    toBeVisitedStack.push(tempNode.right);
                }
            }
        }
        return resultList;
    }

    public List<Integer> postorderTraversal(TreeNode root) {
        Deque<TreeNode> stack = new LinkedList<>();
        stack.push(root);
        List<Integer> ret = new ArrayList<>();
        while (!stack.isEmpty()) {
            TreeNode node = stack.pop();
            if (node != null) {
                ret.add(node.val);
                stack.push(node.left);
                stack.push(node.right);
            }
        }
        Collections.reverse(ret);
        return ret;
    }

    public List<List<Integer>> levelOrder(TreeNode root) {
        List<List<Integer>> resultList = new ArrayList<>();
        int levelNum = 0;//记录某层具有多少个节点
        Queue<TreeNode> treeQueue = new LinkedList<>();
        treeQueue.add(root);
        while (!treeQueue.isEmpty()) {
            levelNum = treeQueue.size();
            List<Integer> levelList = new ArrayList<>();
            while (levelNum > 0) {
                TreeNode tempNode = treeQueue.poll();
                if (tempNode != null) {
                    levelList.add(tempNode.val);
                    treeQueue.add(tempNode.left);
                    treeQueue.add(tempNode.right);
                }
                levelNum--;
            }
            if (levelList.size() > 0)
                resultList.add(levelList);
        }
        return resultList;
    }
}
