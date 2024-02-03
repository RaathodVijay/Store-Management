package com.vijay.orderservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;



@Table(name="order-tbl")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long orderId;
    @Column(name="user_id")
    private String userId;
    @Column(name="product_id")
    private long productId;
    @Column(name="quantity")
    private long quantity;
    @Column(name="order_date")
    private Instant orderDate;
    @Column(name="status")
    private String orderStatus;
    @Column(name = "total_amount")
    private long amount;
}









