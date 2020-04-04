package com.exodus.arhaiyun.fundamentals.datastructure;

import java.util.EmptyStackException;

public class LinkedStack<T> implements Stack<T> {
    //不用容器或者数组等数据结构存储节点
    //Node定义一个节点类
    private static class Node<U> {
        private U item; //存储的data
        private Node<U> next; //类似指针

        Node() {
            this.item = null;
            this.next = null;
        }

        Node(U item, Node<U> next) {
            this.item = item;
            this.next = next;
        }

        boolean end() {
            return item == null && next == null;
        }
    }

    private Node<T> top; //栈顶指针

    LinkedStack() {
        top = new Node<T>();
    }

    //弹栈
    public T pop() {
        if (this.isEmpty()) {
            throw new EmptyStackException();
        }
        T result = top.item;
        if (!top.end()) {
            top = top.next;
        }
        return result;
    }

    //压栈
    public void push(T element) {
        top = new Node<T>(element, top);
    }

    //判断是否为空
    public boolean isEmpty() {
        return top.end();
    }

    //返回栈顶元素
    public T peek() {
        if (this.isEmpty()) {
            throw new EmptyStackException();
        }
        return top.item;
    }
}