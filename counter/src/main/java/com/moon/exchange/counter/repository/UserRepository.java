package com.moon.exchange.counter.repository;

import com.moon.exchange.counter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.relational.core.sql.In;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@Repository
public interface UserRepository extends JpaRepository<User, In> {

    Optional<User> findByUidAndPassword(Long uid, String password);
}
