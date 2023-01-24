package com.moon.exchange.gateway.config;

import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.moon.exchange.common.checksum.ICheckSum;
import com.moon.exchange.common.codec.IBodyCodec;
import com.moon.exchange.common.fetch.IFetchService;
import com.moon.exchange.gateway.cache.OrderCmdContainer;
import com.moon.exchange.gateway.handler.ConnectHandler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;

/**
 * @author Chanmoey
 * @date 2023年01月22日
 */
@Getter
@Log4j2
public class GatewayConfig {

    private short id;

    private int recvPort;

    private int fetchServerPort;

    @Setter
    private IBodyCodec bodyCodec;

    @Setter
    private ICheckSum checkSum;

    private final Vertx vertx = Vertx.vertx();

    public void initConfig(String fileName) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(fileName));
        Element root = document.getRootElement();

        // 解析id
        id = Short.parseShort(root.element("id").getText());

        // 解析端口
        recvPort = Integer.parseInt(root.element("recvport").getText());

        // 解析供排队机拉取数据的端口
        fetchServerPort = Integer.parseInt(root.element("fetchserverport").getText());

        log.info("GateWay ID: {}, Port: {}, FetchServerPort: {}", id, recvPort, fetchServerPort);
    }

    public void startUp() throws Exception {
        // 启动TCP服务监听
        initRecv();

        // 排队机交互
        initFetchServer();
    }

    private void initFetchServer() {
        ServerConfig rpcConfig = new ServerConfig()
                .setPort(fetchServerPort)
                .setProtocol("bolt");

        ProviderConfig<IFetchService> providerConfig = new ProviderConfig<IFetchService>()
                .setInterfaceId(IFetchService.class.getName())
                .setRef(() -> OrderCmdContainer.getInstance().getAll())
                .setServer(rpcConfig);
        providerConfig.export();

        log.info("gateway startup fetchServer success at port: {}", fetchServerPort);
    }

    private void initRecv() {
        NetServer server = vertx.createNetServer();
        server.connectHandler(new ConnectHandler(this));
        server.listen(recvPort, res -> {
            if (res.succeeded()) {
                log.info("gateway startup success at post: {}", recvPort);
            } else {
                log.error("gateway startup fail: {} and recvPort: {}", res, recvPort);
            }
        });
    }
}
