package com.moon.exchange.matching.handler.quotation;

import com.moon.exchange.common.bean.CommonMsg;
import com.moon.exchange.common.order.CmdType;
import com.moon.exchange.common.quotation.L1MarketData;
import com.moon.exchange.common.quotation.MatchData;
import com.moon.exchange.matching.config.MatchingConfig;
import com.moon.exchange.matching.event.RbCmd;
import com.moon.exchange.matching.handler.BaseHandler;
import com.moon.exchange.matching.orderbook.MatchEvent;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.collections.api.tuple.primitive.ShortObjectPair;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;

import java.util.List;

import static com.moon.exchange.common.bean.MsgConstants.*;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Log4j2
@RequiredArgsConstructor
public class L1PubHandler extends BaseHandler {

    /**
     * 每五秒刷新一次行情
     */
    public static final int QUOTATION_RATE = 5000;

    /**
     * key: 柜台id
     * value: 某柜台订阅的撮合数据
     */
    @NonNull
    private final ShortObjectHashMap<List<MatchData>> matcherEventMap;

    @NonNull
    private MatchingConfig config;

    @Override
    public void onEvent(RbCmd rbCmd, long sequence, boolean endOfBatch) throws Exception {
        final CmdType cmdType = rbCmd.command;

        if (cmdType == CmdType.NEW_ORDER || cmdType == CmdType.CANCEL_ORDER) {
            for (MatchEvent e : rbCmd.matchEventList) {
                matcherEventMap.get(e.mid).add(e.copy());
            }
        } else if (cmdType == CmdType.QUOTATION_PUB) {
            // 1. 五档行情
            pubMarketData(rbCmd.marketDataMap);
            // 2. 给柜台发送MatchData
            pubMatcherData();
        }
    }

    public static final short QUOTATION_ADDRESS = -1;

    private void pubMarketData(IntObjectHashMap<L1MarketData> marketDataMap) {
        // 次日志是为了调试方便
        log.info("市场数据" + marketDataMap);
        byte[] serialize = null;
        try {
            serialize = config.getBodyCodec().serialize(marketDataMap.values().toArray(new L1MarketData[0]));
        } catch (Exception e) {
            log.error(e);
        }

        if (serialize == null) {
            return;
        }

        pubData(serialize, QUOTATION_ADDRESS, MATCH_QUOTATION_DATA);
    }

    private void pubData(byte[] serialize, short dst, short msgType) {
        CommonMsg commonMsg = CommonMsg.createCommonMsg(
                serialize,
                config.getCheckSum().getCheckSum(serialize),
                config.getId(),
                dst,
                msgType,
                NORMAL,
                0L
        );

        config.getBusSender().publish(commonMsg);
    }


    private void pubMatcherData() {
        if (matcherEventMap.size() == 0) {
            return;
        }

        // 开发调试日志
        log.info("撮合数据" + matcherEventMap);

        try {
            for (ShortObjectPair<List<MatchData>> s : matcherEventMap.keyValuesView()) {
                if (CollectionUtils.isEmpty(s.getTwo())) {
                    continue;
                }
                byte[] serialize = config.getBodyCodec().serialize(s.getTwo().toArray(new MatchData[0]));
                pubData(serialize, s.getOne(), MATCH_ORDER_DATA);

                // 数据已发送，则清空原来的数据，避免重复发送
                s.getTwo().clear();
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
