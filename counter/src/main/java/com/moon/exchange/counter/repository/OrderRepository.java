package com.moon.exchange.counter.repository;

import com.moon.exchange.counter.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<List<Order>> findOrdersByUid(Long uid);
}
