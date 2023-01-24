package com.moon.exchange.counter.cache;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.moon.exchange.counter.entity.Stock;
import com.moon.exchange.counter.service.StockServiceImpl;
import javax.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月21日
 */
@Component
@NoArgsConstructor
@Log4j2
public class StockCache {

    // 类似Map<String, List<Stock>>
    private final HashMultimap<String, Stock> invertIndex = HashMultimap.create();

    @Autowired
    private StockServiceImpl stockService;

    public Collection<Stock> getStocks(String key) {
        return invertIndex.get(key);
    }

    @PostConstruct
    private void createInvertIndex() {
        log.info("load stock from db");
        long startTime = System.currentTimeMillis();

        // 全表扫描所有股票代码
        List<Stock> res = stockService.getAllStock();
        if (res.isEmpty()) {
            log.error("no stock find in db");
            return;
        } else {
            // 建立倒排索引
            for (Stock stock : res) {
                // 000001 平安银行（payh）
                List<String> codeMetas = splitData(String.format("%6d", stock.getCode()));
                List<String> abbrNameMetas = splitData(stock.getAbbrName());
                codeMetas.addAll(abbrNameMetas);

                // put到map中
                for (String key : codeMetas) {

                    // 限制索引数据列表长度
                    Collection<Stock> stockCollection = invertIndex.get(key);
                    if (stockCollection.size() > 10) {
                        continue;
                    }
                    invertIndex.put(key, stock);
                }
            }
        }

        log.info("load stock finish, take: " +
                (System.currentTimeMillis() - startTime) + "ms");
    }

    /**
     * 对字符进行拆分
     * payh -> p pa pay payh a ay ayh y yh h
     *
     * @param code
     * @return
     */
    private List<String> splitData(String code) {
        List<String> list = Lists.newArrayList();
        int outLength = code.length();
        for (int i = 0; i < outLength; i++) {
            int inLength = outLength + 1;
            for (int j = i + 1; j < inLength; j++) {
                list.add(code.substring(i, j));
            }
        }
        return list;
    }
}
