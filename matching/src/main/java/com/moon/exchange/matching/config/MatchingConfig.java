package com.moon.exchange.matching.config;

import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RegionRouteTableOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MultiRegionRouteTableOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.moon.exchange.common.checksum.ICheckSum;
import com.moon.exchange.common.checksum.XorCheckSum;
import com.moon.exchange.common.codec.BodyCodec;
import com.moon.exchange.common.codec.IBodyCodec;
import com.moon.exchange.common.codec.IMsgCodec;
import com.moon.exchange.common.codec.MsgCodec;
import com.moon.exchange.common.pack.CmdPack;
import com.moon.exchange.matching.cache.CmdPacketQueue;
import com.moon.exchange.matching.core.MatchingApi;
import com.moon.exchange.matching.handler.BaseHandler;
import com.moon.exchange.matching.handler.match.StockMatchHandler;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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
@Component
@PropertySource(value = "classpath:matching.properties")
@Getter
@Log4j2
@ToString
public class MatchingConfig {

    @Value("${id}")
    private short id;

    @Value("${order-recv-ip}")
    private String orderRecvIp;
    @Value("${order-recv-port}")
    private int orderRecvPort;

    @Value("${pub-ip}")
    private String pubIp;
    @Value("${pub-port}")
    private int pubPort;

    @Value("${seq-url-list}")
    private String seqUrlList;

    @Setter
    private IBodyCodec bodyCodec;
    @Setter
    private ICheckSum checkSum;
    @Setter
    private IMsgCodec msgCodec;

    @Autowired
    private MatchingService service;

    @Bean(name = "myMatchingConfig")
    public MatchingConfig getMatchingConfig() {
        MatchingConfig config = new MatchingConfig();
        config.setBodyCodec(new BodyCodec());
        config.setCheckSum(new XorCheckSum());
        config.setMsgCodec(new MsgCodec());
        return config;
    }


    @Getter
    private final MatchingApi matchingApi = new MatchingApi();

    private final Vertx vertx = Vertx.vertx();

    public void startUp() throws Exception {
        log.info("loading config: {}", this);
        startSeqConnection();
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

    }
}
