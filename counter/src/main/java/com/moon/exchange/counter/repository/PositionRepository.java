package com.moon.exchange.counter.repository;

import com.moon.exchange.counter.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月20日
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, Integer> {

    Optional<List<Position>> findPositionsByUid(Long uid);

    Position findPositionByUidAndCode(Long uid, Integer code);
}
