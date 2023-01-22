package com.moon.exchange.gateway.config;

import com.moon.exchange.common.checksum.ICheckSum;
import com.moon.exchange.common.codec.IBodyCodec;
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

    @Setter
    private IBodyCodec bodyCodec;

    @Setter
    private ICheckSum checkSum;

    private Vertx vertx = Vertx.vertx();

    public void initConfig(String fileName) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(fileName));
        Element root = document.getRootElement();

        // 解析id
        id = Short.parseShort(root.element("id").getText());

        // 解析端口
        recvPort = Integer.parseInt(root.element("recvport").getText());

        log.info("GateWay ID: {}, Port: {}", id, recvPort);
    }

    public void startUp() throws Exception {
        // 启动TCP服务监听
        initRecv();
    }

    private void initRecv() {
        NetServer server = vertx.createNetServer();
        server.connectHandler(new ConnectHandler(this));
        server.listen(recvPort, res -> {
            if (res.succeeded()) {
                log.info("gateway startup success at post: {}", recvPort);
            } else {
                log.error("gateway startup fail");
            }
        });
    }
}
