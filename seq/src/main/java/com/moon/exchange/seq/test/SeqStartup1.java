package com.moon.exchange.seq.test;

import com.moon.exchange.seq.config.SeqConfig;

/**
 * @author Chanmoey
 * @date 2023年01月23日
 */
public class SeqStartup1 {

    public static void main(String[] args) throws Exception {
        String configName = "seq1.properties";
        new SeqConfig(configName).startUp();
    }
}
