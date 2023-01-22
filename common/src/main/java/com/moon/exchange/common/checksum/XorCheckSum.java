package com.moon.exchange.common.checksum;

/**
 * 异或，速度快
 *
 * @author Chanmoey
 * @date 2023年01月22日
 */
public class XorCheckSum implements ICheckSum {

    @Override
    public byte getCheckSum(byte[] data) {
        byte sum = 0;

        for (byte b : data) {
            sum ^= b;
        }

        return sum;
    }
}
