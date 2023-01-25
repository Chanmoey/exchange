package com.moon.exchange.matching.handler.risk;

import com.moon.exchange.common.order.CmdType;
import com.moon.exchange.matching.event.CmdResultCode;
import com.moon.exchange.matching.event.RbCmd;
import com.moon.exchange.matching.handler.BaseHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

/**
 * 前置风控处理器
 *
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Log4j2
@RequiredArgsConstructor
public class ExistRiskHandler extends BaseHandler {

    /**
     * 用户账号集合
     */
    @NonNull
    private MutableLongSet uidSet;

    /**
     * 股票代码集合
     */
    @NonNull
    private MutableIntSet codeSet;


    /**
     * 1. 用户是否存在
     * 2. 股票是否合法
     */
    @Override
    public void onEvent(RbCmd rbCmd, long sequence, boolean endOfBatch) throws Exception {

        if (rbCmd.command == CmdType.QUOTATION_PUB) {
            // 行情发布委托不需要进行风险控制
            return;
        }

        if (rbCmd.command == CmdType.NEW_ORDER ||
                rbCmd.command == CmdType.CANCEL_ORDER) {
            // 用户是否合法
            if (!uidSet.contains(rbCmd.uid)) {
                log.error("illegal uid[{}]", rbCmd.uid);
                rbCmd.resultCode = CmdResultCode.RISK_INVALID_USER;
                return;
            }

            // 股票是否合法
            if (!codeSet.contains(rbCmd.code)) {
                log.error("illegal stock code[{}]", rbCmd.code);
                rbCmd.resultCode = CmdResultCode.RISK_INVALID_CODE;
            }
        }
    }
}
