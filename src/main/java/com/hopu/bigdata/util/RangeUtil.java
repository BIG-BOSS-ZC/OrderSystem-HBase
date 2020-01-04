package com.hopu.bigdata.util;

public class RangeUtil {
    public static int getNum(int start, int end) {
        return (int) (Math.random() * (end - start) + start);
    }

    public static Long getRandLong(Long start, Long end) {
        return (long) (Math.random() * (end - start)) + start;
    }
}
