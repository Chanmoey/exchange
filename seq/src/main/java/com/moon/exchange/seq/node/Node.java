package com.moon.exchange.seq.node;

import com.alipay.sofa.jraft.rhea.LeaderStateListener;
import com.alipay.sofa.jraft.rhea.client.DefaultRheaKVStore;
import com.alipay.sofa.jraft.rhea.client.RheaKVStore;
import com.alipay.sofa.jraft.rhea.options.RheaKVStoreOptions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Chanmoey
 * @date 2023年01月23日
 */
@Log4j2
@RequiredArgsConstructor
public class Node {

    @NonNull
    private final RheaKVStoreOptions options;

    private RheaKVStore rheaKVStore;

    private final AtomicLong leaderTerm = new AtomicLong(-1);

    public boolean isLeader() {
        return leaderTerm.get() > 0;
    }

    public void start() {
        // 初始化kvStore
        rheaKVStore = new DefaultRheaKVStore();
        rheaKVStore.init(this.options);

        // 监听kvStore状态
        rheaKVStore.addLeaderStateListener(-1, new LeaderStateListener() {
            @Override
            public void onLeaderStart(long newTerm) {
                log.info("node become leader");
                leaderTerm.set(newTerm);
            }

            @Override
            public void onLeaderStop(long l) {
                leaderTerm.set(-1);
            }
        });
    }

    public void stop() {
        rheaKVStore.shutdown();
    }
}
