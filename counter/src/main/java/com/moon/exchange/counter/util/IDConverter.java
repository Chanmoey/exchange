package com.moon.exchange.counter.util;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
public class IDConverter {

    public static long combineInt2Long(int high, int low) {
        return (((long) high << 32) & 0xFFFFFFFF00000000L) | (low & 0xFFFFFFFFL);
    }

    public static int[] separateLong2Int(long val) {
        int[] res = new int[2];
        res[1] = (int) (0xFFFFFFFFL & val);
        res[0] = (int) ((0xFFFFFFFF00000000L & val) >> 32);
        return res;
    }

    public static void main(String[] args) {
        int high = 1001;
        int low = 200;
        long l = IDConverter.combineInt2Long(high, low);
        System.out.println(l);

        int[]res = IDConverter.separateLong2Int(l);
        System.out.println("high: " + res[0]);
        System.out.println("low: " + res[1]);
    }
}
