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
import com.moon.exchange.seq.node.Node;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Chanmoey
 * @date 2023年01月23日
 */
@Log4j2
@ToString
@RequiredArgsConstructor
public class SeqConfig {

    private String dataPath;

    private String serveUrl;

    private String serverList;

    @NonNull
    private String fileName;

    @Getter
    private Node node;

    public void startUp() throws Exception {
        // 读取配置文件
        initConfig();

        // 初始化kv store集群
        startSeqDbCluster();
    }

    private void initConfig() throws IOException {
        Properties properties = new Properties();

        properties.load(SeqConfig.class.getResourceAsStream("/" + fileName));

        dataPath = properties.getProperty("data-path");
        serveUrl = properties.getProperty("serve-url");
        serverList = properties.getProperty("server-list");

        log.info("read config: {}", this);
    }

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
}
