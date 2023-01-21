package com.moon.exchange.counter.repository;

import com.moon.exchange.counter.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    @Query("select s.name from Stock s where s.code = :code")
    Optional<String> getNameByCode(Integer code);

}
