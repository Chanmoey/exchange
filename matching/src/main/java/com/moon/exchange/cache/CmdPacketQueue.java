package com.moon.exchange.cache;

import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.storage.KVEntry;
import com.alipay.sofa.jraft.util.Bits;
import com.moon.exchange.common.codec.IBodyCodec;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.common.pack.CmdPack;
import com.moon.exchange.core.MatchingApi;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author Chanmoey
 * @date 2023年01月25日
 */
@Log4j2
public class CmdPacketQueue {

    /**
     * 单例，饿汉式
     */
    private static CmdPacketQueue INSTANCE = new CmdPacketQueue();

    private CmdPacketQueue() {
    }

    public static CmdPacketQueue getInstance() {
        return INSTANCE;
    }

    private final BlockingQueue<CmdPack> recvCache = new LinkedBlockingDeque<>();

    public void cache(CmdPack pack) {
        recvCache.offer(pack);
    }

    private RheaKVStore orderKVStore;
    private IBodyCodec codec;
    private MatchingApi matchingApi;

    public void init(RheaKVStore orderKVStore, IBodyCodec codec, MatchingApi matchingApi) {
        this.orderKVStore = orderKVStore;
        this.codec = codec;
        this.matchingApi = matchingApi;

        new Thread(() -> {
            while (true) {
                try {
                    CmdPack cmdPack = recvCache.poll(10, TimeUnit.SECONDS);
                    if (cmdPack != null) {
                        handle(cmdPack);
                    }
                } catch (Exception e) {
                    log.error("msg packet recv cache error, continue", e);
                }
            }
        }).start();
    }

    private long lastPackNo = -1;

    /**
     * 包处理逻辑，单线程
     *
     * @param cmdPack 委托数据报
     */
    private void handle(CmdPack cmdPack) throws Exception {
        log.info("recv: {}", cmdPack);

        // NACK，校验包号
        long packetNo = cmdPack.getPackNo();
        if (packetNo == lastPackNo + 1) {
            // 正确的包
            if (CollectionUtils.isEmpty(cmdPack.getOrderCmdList())) {
                return;
            }
            for (OrderCmd orderCmd : cmdPack.getOrderCmdList()) {
                matchingApi.submitCommand(orderCmd);
            }
        } else if (packetNo <= lastPackNo) {
            // 收到历史重复的包，记录日志，并忽略掉
            log.warn("recv duplicate pack id: {}", packetNo);
        } else {
            // 跳号了，出现了包遗失
            log.info("packNo last from {} to {}, begin query form sequencer", lastPackNo + 1, packetNo);
            // 主动请求排队机
            byte[] firstKey = new byte[8];
            Bits.putLong(firstKey, 0, lastPackNo + 1);
            byte[] lastKey = new byte[8];
            Bits.putLong(lastKey, 0, packetNo + 1);
            // 请求区间数据
            final List<KVEntry> kvEntries = orderKVStore.bScan(firstKey, lastKey);
            if (CollectionUtils.isNotEmpty(kvEntries)) {
                List<CmdPack> collect = new ArrayList<>();
                for (KVEntry entry : kvEntries) {
                    byte[] value = entry.getValue();
                    if (ArrayUtils.isNotEmpty(value)) {
                        collect.add(codec.deserialize(value, CmdPack.class));
                    }
                }
                collect.sort((o1, o2) -> (int) (o1.getPackNo() - o2.getPackNo()));
                // 传到撮合核心
                for (CmdPack pack : collect) {
                    if (CollectionUtils.isEmpty(pack.getOrderCmdList())) {
                        continue;
                    }
                    for (OrderCmd orderCmd : pack.getOrderCmdList()) {
                        matchingApi.submitCommand(orderCmd);
                    }
                }
            } else {
                // 排队机出现故障，不再理睬中间的缺失数据
                lastPackNo = packetNo;
            }
        }
    }
}
