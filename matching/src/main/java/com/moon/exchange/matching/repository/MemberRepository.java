package com.moon.exchange.matching.repository;

import com.moon.exchange.matching.entity.Member;
import com.moon.exchange.matching.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Chanmoey
 * @date 2023年01月16日
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

    @Query("select m.mid from Member m")
    List<Short> getAllMid();
}
