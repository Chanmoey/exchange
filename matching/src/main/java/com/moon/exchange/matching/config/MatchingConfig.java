package com.moon.exchange.matching.config;

import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.moon.exchange.common.bus.IBusSender;
import com.moon.exchange.common.bus.MqttBusSender;
import com.moon.exchange.common.checksum.ICheckSum;
import com.moon.exchange.common.checksum.XorCheckSum;
import com.moon.exchange.common.codec.BodyCodec;
import com.moon.exchange.common.codec.IBodyCodec;
import com.moon.exchange.common.codec.IMsgCodec;
import com.moon.exchange.common.codec.MsgCodec;
import com.moon.exchange.common.pack.CmdPack;
import com.moon.exchange.common.quotation.MatchData;
import com.moon.exchange.matching.cache.CmdPacketQueue;
import com.moon.exchange.matching.core.MatchingApi;
import com.moon.exchange.matching.core.MatchingCore;
import com.moon.exchange.matching.handler.BaseHandler;
import com.moon.exchange.matching.handler.match.StockMatchHandler;
import com.moon.exchange.matching.handler.quotation.L1PubHandler;
import com.moon.exchange.matching.handler.risk.ExistRiskHandler;
import com.moon.exchange.matching.orderbook.GOrderBookImpl;
import com.moon.exchange.matching.orderbook.IOrderBook;
import com.moon.exchange.matching.service.MatchingService;
import io.netty.util.collection.IntObjectHashMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月25日
 */
@Getter
@Log4j2
@ToString
public class MatchingConfig {

    public MatchingConfig(MatchingService service) {
        this.service = service;
    }

    private short id = 1003;
    private String orderRecvIp = "230.0.0.1";
    private int orderRecvPort = 1234;
    private String pubIp = "192.168.40.30";
    private int pubPort = 1883;
    private String seqUrlList = "127.0.0.1:8891,127.0.0.1:8892,127.0.0.1:8893";

    @Setter
    private IBodyCodec bodyCodec;
    @Setter
    private ICheckSum checkSum;
    @Setter
    private IMsgCodec msgCodec;

    private MatchingService service;

    @Getter
    private MatchingApi matchingApi;

    private final Vertx vertx = Vertx.vertx();

    public void startUp() throws Exception {
        // 启动撮合核心
        startMatching();

        // 建立连接总线，初始化数据的发送
        initPub();

        // 初始化排队机的连接
        startSeqConnection();

        log.info("loading config: {}", this);
    }

    @Getter
    @ToString.Exclude
    private final RheaKVStore orderKVStore = new DefaultRheaKVStore();

    /**
     * 连接排队机（KVStore）
     *
     * @throws Exception 异常
     */
    private void startSeqConnection() throws Exception {
        final List<RegionRouteTableOptions> regionRouteTableOptionsList
                = MultiRegionRouteTableOptionsConfigured
                .newConfigured()
                .withInitialServerList(-1L, seqUrlList)
                .config();

        final PlacementDriverOptions pdOpts =
                PlacementDriverOptionsConfigured
                        .newConfigured()
                        .withFake(true)
                        .withRegionRouteTableOptionsList(regionRouteTableOptionsList)
                        .config();

        final RheaKVStoreOptions opts =
                RheaKVStoreOptionsConfigured
                        .newConfigured()
                        .withPlacementDriverOptions(pdOpts)
                        .config();

        orderKVStore.init(opts);

        // 委托指令处理器
        CmdPacketQueue.getInstance().init(orderKVStore, bodyCodec, matchingApi);

        // 接受缓存数据，使用组播
        DatagramSocket udpSocket = vertx.createDatagramSocket(new DatagramSocketOptions());
        udpSocket.listen(orderRecvPort, "0.0.0.0", asyncRes -> {
            if (asyncRes.succeeded()) {

                udpSocket.handler(packet -> {
                    // 收到包、解包、放入缓存
                    Buffer udpData = packet.data();
                    if (udpData.length() > 0) {
                        try {
                            CmdPack cmdPack = bodyCodec.deserialize(udpData.getBytes(), CmdPack.class);
                            CmdPacketQueue.getInstance().cache(cmdPack);
                        } catch (Exception e) {
                            log.error("decode packet error", e);
                        }

                    } else {
                        log.error("recv empty udp packet from client: {}", packet.sender().toString());
                    }
                });

                try {
                    udpSocket.listenMulticastGroup(
                            orderRecvIp,
                            mainInterface().getName(),
                            null,
                            asyncRes2 -> log.info("listen success {}", asyncRes2.succeeded())
                    );
                } catch (Exception e) {
                    log.error(e);
                }
            } else {
                log.error("Listen failed, ", asyncRes.cause());
            }
        });
    }

    private static NetworkInterface mainInterface() throws Exception {
        // 非loopback、支持multicast、非虚拟网卡、有ipv4
        final ArrayList<NetworkInterface> interfaces =
                Collections.list(NetworkInterface.getNetworkInterfaces());
        return interfaces.stream().filter(i -> {
            try {
                final boolean isLoopBack = i.isLoopback();
                final boolean supportMulticast = i.supportsMulticast();
                final boolean isVirtualBox = i.getDisplayName().contains("VirtualBox")
                        || i.getDisplayName().contains("Host-only");
                final boolean hasIpv4 = i.getInterfaceAddresses()
                        .stream().anyMatch(ii -> ii.getAddress() instanceof Inet4Address);
                return !isLoopBack && supportMulticast && !isVirtualBox && hasIpv4;
            } catch (Exception e) {
                log.error("fine net interface error", e);
            }
            return false;
        }).min(Comparator.comparing(NetworkInterface::getName)).orElse(null);
    }

    /**
     * 启动撮合引擎
     */
    private void startMatching() {
        // 1. 前置风控处理器
        final BaseHandler riskHandler = new ExistRiskHandler(
                service.getAllUid(),
                service.getAllStockCode()
        );

        // 2. 撮合处理器
        // 给每个股票代码，生成一个订单簿OrderBook
        IntObjectHashMap<IOrderBook> orderBookMap = new IntObjectHashMap<>();
        service.getAllStockCode().forEach(code -> orderBookMap.put(code, new GOrderBookImpl(code)));
        final BaseHandler matchHandler = new StockMatchHandler(orderBookMap);

        // 3. 发布处理器
        ShortObjectHashMap<List<MatchData>> matcherEventMap =
                new ShortObjectHashMap<>();
        for (short mid : service.getAllCounterMid()) {
            matcherEventMap.put(mid, new ArrayList<>());
        }
        final BaseHandler pubHandler = new L1PubHandler(matcherEventMap, this);

        matchingApi = new MatchingCore(
                riskHandler,
                matchHandler,
                pubHandler
        ).getApi();
    }

    @Getter
    private IBusSender busSender;

    /**
     * 初始化连接总线
     */
    private void initPub() {
        busSender = new MqttBusSender(pubIp, pubPort, msgCodec, vertx);
        busSender.startUp();
    }
}
