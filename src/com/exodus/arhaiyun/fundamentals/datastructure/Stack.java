package com.exodus.arhaiyun.fundamentals.datastructure;

public interface Stack<T> {
    public T pop();

    public void push(T element);

    public boolean isEmpty();

    public T peek();
}