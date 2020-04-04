package com.exodus.arhaiyun.fundamentals.binarytree;

import com.exodus.arhaiyun.alibaba.CountWordsThread;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

public class DealFileText {
    private File file;
    private int threadNum;
    private Vector<CountWordsThread> listCountWordsThreads;
    private Vector<Thread> listThread;
    private long splitSize;
    private long currentPos;

    public DealFileText(File file, int threadNum, long splitSize) {
        this.file = file;
        this.threadNum = threadNum;
        this.splitSize = splitSize;
        this.currentPos = 0;
        this.listCountWordsThreads = new Vector<CountWordsThread>();
        this.listThread = new Vector<Thread>();
    }

    public void doFile() throws IOException {
        while (currentPos < this.file.length()) {
            for (int num = 0; num < threadNum; num++) {
                if (currentPos < file.length()) {
                    CountWordsThread countWordsThread = null;
                    if (currentPos + splitSize < file.length()) {
                        RandomAccessFile raf = new RandomAccessFile(file, "r");
                        raf.seek(currentPos + splitSize);
                        int offset = 0;
                        while (true) {
                            char ch = (char) raf.read();
                            //是否到文件末尾，到了跳出
                            //是否是字母和'，都不是跳出（防止单词被截断）
                            if (!Character.isLetter(ch) && '\'' != ch)
                                break;
                            offset++;
                        }
                        countWordsThread = new CountWordsThread(file, currentPos, splitSize + offset);
                        currentPos += splitSize + offset;
                        raf.close();
                    } else {
                        countWordsThread = new CountWordsThread(file, currentPos, file.length() - currentPos);
                        currentPos = file.length();
                    }
                    Thread thread = new Thread(countWordsThread);
                    thread.start();
                    listCountWordsThreads.add(countWordsThread);
                    listThread.add(thread);
                }
            }

            //判断线程是否执行完成
            while (true) {
                boolean threadsDone = true;
                for (Thread thread : listThread) {
                    if (thread.getState() != Thread.State.TERMINATED) {
                        threadsDone = false;
                        break;
                    }
                }
                if (threadsDone)
                    break;
            }
        }

        //当分别统计的线程结束后，开始统计总数目的线程
        new Thread(() ->
        {
            // 使用TreeMap保证结果有序（按首字母排序）
            TreeMap<String, Integer> tMap = new TreeMap<String, Integer>();
            for (CountWordsThread listCountWordsThread : listCountWordsThreads) {
                Map<String, Integer> hMap = listCountWordsThread.getResultMap();
                Set<String> keys = hMap.keySet();
                for (String key : keys) {
                    if (key.equals(""))
                        continue;
                    if (tMap.get(key) == null) {
                        tMap.put(key, hMap.get(key));
                    } else {
                        tMap.put(key, tMap.get(key) + hMap.get(key));
                    }
                }
            }

            for (Thread thread : listThread) {
                thread.interrupt();
            }

            Set<String> keys = tMap.keySet();
            for (String key : keys) {
                System.out.println(key + ": " + tMap.get(key));
            }
        }).start();
    }
}