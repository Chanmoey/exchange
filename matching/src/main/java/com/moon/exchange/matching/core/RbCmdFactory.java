package com.moon.exchange.matching.core;

import com.lmax.disruptor.EventFactory;
import com.moon.exchange.matching.event.CmdResultCode;
import com.moon.exchange.matching.event.RbCmd;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.util.ArrayList;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
public class RbCmdFactory implements EventFactory<RbCmd> {
    @Override
    public RbCmd newInstance() {
        return RbCmd.builder()
                .resultCode(CmdResultCode.SUCCESS)
                .matchEventList(new ArrayList<>())
                .marketDataMap(new IntObjectHashMap<>())
                .build();
    }
}
