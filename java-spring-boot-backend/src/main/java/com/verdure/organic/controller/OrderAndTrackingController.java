package com.verdure.organic.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:3000")
public class OrderAndTrackingController {

    private final List<Map<String, Object>> orders = new ArrayList<>();

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUserOrders(@RequestParam(required = false) String userId) {
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> order = new HashMap<>(request);
        order.put("orderId", "ORD-VL-" + (10000 + new Random().nextInt(89999)));
        order.put("status", "ACTIVE");
        order.put("currentStageIndex", 0);
        orders.add(0, order);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
