package com.moon.exchange.matching.repository;

import com.moon.exchange.matching.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    @Query("select u.uid from User u")
    List<Long> getAllUid();

    @Query("select u.balance from User u where u.uid = :uid")
    Optional<Long> getBalanceByUid(Long uid);
}
