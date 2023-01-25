package com.moon.exchange.seq.task;

import com.alipay.sofa.jraft.util.Bits;
import com.alipay.sofa.jraft.util.BytesUtil;
import com.moon.exchange.common.fetch.IFetchService;
import com.moon.exchange.common.order.OrderCmd;
import com.moon.exchange.common.order.OrderDirection;
import com.moon.exchange.seq.config.SeqConfig;
import com.moon.exchange.seq.pack.CmdPack;
import io.vertx.core.buffer.Buffer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

/**
 * @author Chanmoey
 * @date 2023年01月24日
 */
@Log4j2
@RequiredArgsConstructor
public class FetchTask extends TimerTask {

    @NonNull
    private SeqConfig config;

    @Override
    public void run() {
        // ***遍历所有网关获取数据***
        if (!config.getNode().isLeader()) {
            //不是主节点，不需要去拉取数据
            return;
        }

        Map<String, IFetchService> fetchServiceMap = config.getFetchServiceMap();

        if (MapUtils.isEmpty(fetchServiceMap)) {
            return;
        }

        // ***获取数据***
        List<OrderCmd> cmdList = collectAllOrders(fetchServiceMap);
        if (CollectionUtils.isEmpty(cmdList)) {
            return;
        }

        log.info("fetch data from gateway: {}", cmdList);

        // ***对数据进行排序: 时间优先，价格优先（买价高优先，卖价低优先）、量优先***
        cmdList.sort(((o1, o2) -> {
            int compare = compareTime(o1, o2);
            if (compare != 0) {
                return compare;
            }
            compare = comparePrice(o1, o2);
            if (compare != 0) {
                return compare;
            }
            return compareVolume(o1, o2);
        }));

        // ***存到 KVStore，发送到撮合核心。***
        try {
            // 1. 生成全局唯一的packetNo
            long packetNo = getPacketNoFromStore();
            // 2. 入库
            CmdPack pack = new CmdPack(packetNo, cmdList);
            insertToKVStore(packetNo, config.getCodec().serialize(pack));
            // 3. 更新packetNo（+1）
            updatePacketNoInStore(packetNo + 1);
            // 4. 发送广播，null为异步处理器，包一经发送则调用这个处理器，
            // 所以无法知道下游有没有正确收到，因此没必要设置异步处理器。
            byte[] serialize = config.getCodec().serialize(pack);
            config.getMulticastSender()
                    .send(
                            Buffer.buffer(serialize),
                            config.getMulticastPort(),
                            config.getMulticastIp(),
                            null
                    );


        } catch (Exception e) {
            log.info("encode cmd packet error", e);
        }
    }


    private static final byte[] PACKET_NO_KEY = BytesUtil.writeUtf8("seq_packet_no");

    /**
     * 获得包序号packetNo，包需要需要全局唯一，所以从KVStore中获取
     *
     * @return packetNo
     */
    private long getPacketNoFromStore() {
        final byte[] bPacketNo = config.getNode().getRheaKVStore().bGet(PACKET_NO_KEY);

        long packetNo = 0;
        if (ArrayUtils.isNotEmpty(bPacketNo)) {
            packetNo = Bits.getLong(bPacketNo, 0);
        }
        return packetNo;
    }

    /**
     * 更新包号
     *
     * @param newPacketNo 包号
     */
    private void updatePacketNoInStore(long newPacketNo) {
        final byte[] bytes = new byte[8];
        Bits.putLong(bytes, 0, newPacketNo);
        config.getNode().getRheaKVStore().put(PACKET_NO_KEY, bytes);
    }

    /**
     * 将委托数据的封装包保存到KVStore中
     *
     * @param packetNo  包号
     * @param serialize 序列化数据
     */
    private void insertToKVStore(long packetNo, byte[] serialize) {
        byte[] key = new byte[8];
        Bits.putLong(key, 0, packetNo);
        config.getNode().getRheaKVStore().put(key, serialize);
    }

    private int compareTime(OrderCmd o1, OrderCmd o2) {
        return Long.compare(o1.timestamp, o2.timestamp);
    }

    private int comparePrice(OrderCmd o1, OrderCmd o2) {
        if (o1.direction == o2.direction) {
            if (o1.price > o2.price) {
                // 买委托，价格高排前面，返回-1，表示要排在前面。
                return o1.direction == OrderDirection.BUY ? -1 : 1;
            } else if (o1.price < o2.price) {
                return o1.direction == OrderDirection.BUY ? 1 : -1;
            } else {
                // 价格相同，无法排序
                return 0;
            }
        }

        // 方向不同，无法排序。
        return 0;
    }

    private int compareVolume(OrderCmd o1, OrderCmd o2) {
        // 量高优先
        return Long.compare(o2.volume, o1.volume);
    }

    private List<OrderCmd> collectAllOrders(Map<String, IFetchService> fetchServiceMap) {
        List<OrderCmd> msgList = new ArrayList<>();
        fetchServiceMap.values().forEach(t -> {
            List<OrderCmd> orderCmdList = t.fetchData();
            if (CollectionUtils.isNotEmpty(orderCmdList)) {
                msgList.addAll(orderCmdList);
            }
        });

        return msgList;
    }
}
