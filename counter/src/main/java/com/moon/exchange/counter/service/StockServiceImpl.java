package com.moon.exchange.counter.service;

import com.moon.exchange.counter.exception.bussness.NoFountException;
import com.moon.exchange.counter.repository.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Service
public class StockServiceImpl {

    @Autowired
    private StockRepository stockRepository;

    public String getNameByCode(Long code) {
        return stockRepository.getNameByCode(code)
                .orElseThrow(() -> new NoFountException(20001));
    }
}
