package com.vijay.orderservice.external.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {


    private String name;
    private long productId;
    private String categoryId;
    private long price;
    private long quantity;
}