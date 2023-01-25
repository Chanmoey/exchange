package com.moon.exchange.matching.service;

import com.moon.exchange.matching.repository.StockRepository;
import com.moon.exchange.matching.repository.UserRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@Service
public class MatchingService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private UserRepository userRepository;

    public LongHashSet getAllUid() {
        List<Long> uidList = userRepository.getAllUid();
        if (CollectionUtils.isEmpty(uidList)) {
            throw new RuntimeException("empty user");
        }

        LongHashSet set = new LongHashSet();
        for (Long uid : uidList) {
            set.addAll(uid);
        }

        return set;
    }

    public IntHashSet getAllStockCode() {
        List<Integer> codeList = stockRepository.getAllStockCode();
        if (CollectionUtils.isEmpty(codeList)) {
            throw new RuntimeException("empty stock");
        }

        IntHashSet set = new IntHashSet();
        for (Integer code : codeList) {
            set.addAll(code);
        }
        return set;
    }

    public Long getBalance(Long uid) {
        return userRepository.getBalanceByUid(uid)
                .orElseThrow(
                        () -> new IllegalArgumentException("No such user with uid: " + uid));
    }
}
