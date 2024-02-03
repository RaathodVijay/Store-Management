package com.vijay.orderservice.service;

import com.vijay.orderservice.model.OrderRequest;
import com.vijay.orderservice.model.OrderResponse;

public interface OrderService {
    long placeOrder(OrderRequest orderRequest);

    OrderResponse getDetailsByOrderId(Long orderId);
}
