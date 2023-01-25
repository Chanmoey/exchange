package com.moon.exchange.seq.config;

import com.alipay.sofa.jraft.rhea.options.PlacementDriverOptions;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import com.alipay.sofa.jraft.rhea.options.StoreEngineOptions;
import com.alipay.sofa.jraft.rhea.options.configured.MemoryDBOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.PlacementDriverOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.RheaKVStoreOptionsConfigured;
import com.alipay.sofa.jraft.rhea.options.configured.StoreEngineOptionsConfigured;
import com.alipay.sofa.jraft.rhea.storage.StorageType;
import com.alipay.sofa.jraft.util.Endpoint;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.listener.ChannelListener;
import com.alipay.sofa.rpc.transport.AbstractChannel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.moon.exchange.common.codec.IBodyCodec;
import com.moon.exchange.common.fetch.IFetchService;
import com.moon.exchange.seq.node.Node;
import com.moon.exchange.seq.task.FetchTask;
import io.vertx.core.Vertx;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;

/**
 * @author Chanmoey
 * @date 2023年01月23日
 */
@Log4j2
@ToString
@RequiredArgsConstructor
public class SeqConfig {

    /**
     * 分布式强一致性数据库配置
     */
    private String dataPath;
    private String serveUrl;
    private String serverList;

    /**
     * 网关抓取配置
     */
    private String fetchUrls;

    /**
     * 下游广播配置
     */
    @Getter
    private String multicastIp;
    @Getter
    private int multicastPort;
    @Getter
    private DatagramSocket multicastSender;

    @NonNull
    private String fileName;

    @ToString.Exclude
    @Getter
    private final Map<String, IFetchService> fetchServiceMap = Maps.newConcurrentMap();

    @ToString.Exclude
    @NonNull
    @Getter
    private IBodyCodec codec;

    @Getter
    private Node node;

    public void startUp() throws Exception {
        // 读取配置文件
        initConfig();

        // 初始化kv store集群
        startSeqDbCluster();

        // 从网关抓取委托
        startUpFetch();
    }

    /**
     * 从配置文件中读取数据
     */
    private void initConfig() throws IOException {
        Properties properties = new Properties();

        properties.load(SeqConfig.class.getResourceAsStream("/" + fileName));

        dataPath = properties.getProperty("data-path");
        serveUrl = properties.getProperty("serve-url");
        serverList = properties.getProperty("server-list");
        fetchUrls = properties.getProperty("fetch-urls");
        multicastIp = properties.getProperty("multicast-ip");
        multicastPort = Integer.parseInt(properties.getProperty("multicast-port"));

        log.info("read config: {}", this);
    }

    /**
     * 启动排队机数据库集群
     */
    private void startSeqDbCluster() {
        final PlacementDriverOptions pdOpts = PlacementDriverOptionsConfigured
                .newConfigured()
                .withFake(true)
                .config();

        String[] ipAndPort = serveUrl.split(":");
        final StoreEngineOptions storeOpts = StoreEngineOptionsConfigured
                .newConfigured()
                .withStorageType(StorageType.Memory)
                .withMemoryDBOptions(MemoryDBOptionsConfigured.newConfigured().config())
                .withRaftDataPath(dataPath)
                .withServerAddress(new Endpoint(ipAndPort[0], Integer.parseInt(ipAndPort[1])))
                .config();

        final RheaKVStoreOptions opts = RheaKVStoreOptionsConfigured.newConfigured()
                .withInitialServerList(serverList)
                .withStoreEngineOptions(storeOpts)
                .withPlacementDriverOptions(pdOpts)
                .config();

        node = new Node(opts);
        node.start();

        Runtime.getRuntime().addShutdownHook(new Thread(node::stop));
        log.info("start seq node success on port: {}", ipAndPort[1]);
    }

    /**
     * 启动与网关的连接，并开启定时任务，每5秒从网关抓取最新委托
     */
    private void startUpFetch() {
        // 建立与所有网关的连接
        String[] urls = fetchUrls.split(";");
        for (String url : urls) {
            ConsumerConfig<IFetchService> consumerConfig = new ConsumerConfig<IFetchService>()
                    .setInterfaceId(IFetchService.class.getName()) // 通信接口
                    .setProtocol("bolt") // RPC通信协议
                    .setTimeout(5000) // 超时时间
                    .setDirectUrl(url); // 直连地址
            consumerConfig.setOnConnect(Lists.newArrayList(new FetchChannelListListener(consumerConfig)));
            // 第一次连接时候，不会进入监听器的onConnected方法中，所以这里需要手动put进去
            fetchServiceMap.put(url, consumerConfig.refer());
        }

        // 启动定时任务，定时抓取数据
        new Timer().schedule(new FetchTask(this), 5000, 1000);
    }

    /**
     * RPC通信的监听器
     */
    @RequiredArgsConstructor
    private class FetchChannelListListener implements ChannelListener {

        @NonNull
        private ConsumerConfig<IFetchService> config;

        @Override
        public void onConnected(AbstractChannel channel) {
            String remoteAddr = channel.remoteAddress().toString();
            log.info("connect to gateway: {}", remoteAddr);
            fetchServiceMap.put(remoteAddr, config.refer());
        }

        @Override
        public void onDisconnected(AbstractChannel channel) {
            String remoteAddr = channel.remoteAddress().toString();
            log.info("disconnect to gateway: {}", remoteAddr);
            fetchServiceMap.remove(remoteAddr);
        }
    }

    /**
     * 启动广播器
     */
    private void startMulticast() {
        multicastSender = Vertx.vertx().createDatagramSocket(new DatagramSocketOptions());
    }
}
