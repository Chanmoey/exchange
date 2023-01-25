package com.moon.exchange.matching.repository;

import com.moon.exchange.matching.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("select s.code from Stock s ")
    List<Integer> getAllStockCode();
}
