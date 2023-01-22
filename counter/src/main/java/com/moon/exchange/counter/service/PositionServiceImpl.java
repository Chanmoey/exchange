package com.moon.exchange.counter.service;

import com.moon.exchange.counter.cache.CacheType;
import com.moon.exchange.counter.cache.RedisStringCache;
import com.moon.exchange.counter.entity.Position;
import com.moon.exchange.counter.exception.bussness.NoFountException;
import com.moon.exchange.counter.exception.bussness.OrderException;
import com.moon.exchange.counter.repository.PositionRepository;
import com.moon.exchange.counter.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Service
public class PositionServiceImpl {

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private StockServiceImpl stockService;

    public List<Position> getPositionList(Long uid) {
        // 先查缓存
        String uidStr = uid.toString();
        String positionStr = RedisStringCache.get(uidStr, CacheType.POSITION);

        if (StringUtils.isEmpty(positionStr)) {
            // 查数据库
            List<Position> dbList = positionRepository.findPositionsByUid(uid)
                    .orElseThrow(() -> new NoFountException(20000));
            // 查询stock的name
            this.setStockName(dbList);
            // 更新缓存
            RedisStringCache.cache(uidStr, JsonUtil.toJson(dbList), CacheType.POSITION);
            return dbList;
        } else {
            return JsonUtil.fromJsonArr(positionStr, Position.class);
        }
    }

    public void addPosition(Long uid, Integer code, Long volume, Long price) {
        Position position = positionRepository.findPositionByUidAndCode(uid, code);
        if (position == null) {
            // 原来没有持仓，新增持仓
            Position newPosition = new Position();
            newPosition.setUid(uid);
            newPosition.setCode(code);
            newPosition.setCount(volume);
            newPosition.setCost(price);
            positionRepository.save(newPosition);
        } else {
            // 修改持仓
            if (position.getCost() + volume < 0) {
                throw new OrderException(30003);
            }
            position.setCount(position.getCount() + volume);
            // 这里的总花费
            position.setCost(position.getCost() + price * volume);
            positionRepository.save(position);
        }
    }

    public void minusPosition(Long uid, Integer code, Long volume, Long price) {
        this.addPosition(uid, code, -volume, -price);
    }

    private void setStockName(List<Position> positions) {
        positions.forEach(position -> position.setName(stockService.getNameByCode(position.getCode())));
    }
}
