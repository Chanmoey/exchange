package com.moon.exchange.matching.controller;

import com.moon.exchange.matching.service.MatchingService;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.eclipse.collections.impl.set.mutable.primitive.LongHashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Chanmoey
 * @date 2023年01月26日
 */
@RestController
public class TestController {

    @Autowired
    private MatchingService service;

    @GetMapping("/test")
    public IntHashSet test() {
        return service.getAllStockCode();
    }

    @GetMapping("/test1")
    public LongHashSet test1() {
        LongHashSet allUid = service.getAllUid();
        return allUid;
    }
}
