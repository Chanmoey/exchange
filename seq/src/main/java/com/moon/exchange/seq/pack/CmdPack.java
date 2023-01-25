package com.moon.exchange.seq.pack;

import com.moon.exchange.common.order.OrderCmd;
import lombok.*;

import java.io.Serializable;
import java.util.List;

/**
 * 这是一个网络传输数据报，封装从网关收集的委托数据，发送到下游撮合系统
 *
 * @author Chanmoey
 * @date 2023年01月25日
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CmdPack implements Serializable {

    /**
     * 包号，保证数据的正确性，如果下游收到的包号不是连续的，则需要向排队机获取
     */
    private long packNo;

    /**
     * 保存委托数据
     */
    private List<OrderCmd> orderCmdList;
}
