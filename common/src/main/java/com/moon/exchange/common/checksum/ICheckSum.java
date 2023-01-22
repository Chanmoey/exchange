package com.moon.exchange.common.checksum;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
public interface ICheckSum {

    byte getCheckSum(byte[] data);
}
