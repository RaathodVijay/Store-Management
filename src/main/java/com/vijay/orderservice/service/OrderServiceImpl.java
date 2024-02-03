package com.vijay.orderservice.service;

import com.vijay.orderservice.entity.Order;
import com.vijay.orderservice.exception.CustomException;
import com.vijay.orderservice.external.client.PaymentService;
import com.vijay.orderservice.external.client.ProductService;
import com.vijay.orderservice.external.request.PaymentRequest;
import com.vijay.orderservice.external.response.PaymentResponse;
import com.vijay.orderservice.external.response.ProductResponse;
import com.vijay.orderservice.external.response.UserResponse;
import com.vijay.orderservice.model.OrderRequest;
import com.vijay.orderservice.model.OrderResponse;
import com.vijay.orderservice.repository.OrderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;


@Service
@Log4j2
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private RestTemplate restTemplate;


    @Override
    public long placeOrder(OrderRequest orderRequest) {

        log.info("Placing Order Request: {}", orderRequest);
        productService.reduceQuantity(orderRequest.getProductId(), orderRequest.getQuantity());

        log.info("Creating order with status CREATED");
        Order order
                = Order.builder()
                .amount(orderRequest.getAmount())
                .orderStatus("CREATED")
                .productId(orderRequest.getProductId())
                .orderDate(Instant.now())
                .quantity(orderRequest.getQuantity())
                .userId(orderRequest.getUserId())
                .build();

        orderRepository.save(order);
        log.info("Calling Payment Service to complete the payment");
        PaymentRequest paymentRequest
                = PaymentRequest.builder()
                .orderId(order.getOrderId())
                .paymentMode(orderRequest.getPaymentMode())
                .amount(orderRequest.getAmount())
                .build();
        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done Successfully. Changing the Oder status to PLACED");
            orderStatus = "PLACED";
        } catch (Exception e) {
            log.error("Error occurred in payment. Changing order status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);
        log.info("Order Places successfully with Order Id: {}", order.getOrderId());
        return order.getOrderId();
    }

    @Override
    public OrderResponse getDetailsByOrderId(Long orderId) {
        Order order= orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException("Order not found for the order Id:" + orderId,
                        "NOT_FOUND",
                        404));

        log.info("Invoking Product service to fetch the product for id: {}", order.getProductId());
        ProductResponse productResponse
                = restTemplate.getForObject(
                "http://PRODUCT-SERVICE/product/" + order.getProductId(),
                ProductResponse.class
        );

        log.info("Getting payment information form the payment Service");
        PaymentResponse paymentResponse
                = restTemplate.getForObject(
                "http://PAYMENT-SERVICE/payment/order/" + order.getOrderId(),
                PaymentResponse.class
        );
        log.info("Invoking Product service to fetch the product for id: {}", order.getUserId());
        UserResponse userResponse
                = restTemplate.getForObject(
                "http://USER-SERVICE/user/" + order.getUserId(),
                UserResponse.class
        );

        OrderResponse.UserDetails userDetails
                =OrderResponse.UserDetails.builder()
                .name(userResponse.getName())
                .email(userResponse.getEmail())
                .gender(userResponse.getGender())
                .userId(userResponse.getUserId())
                .build();

        OrderResponse.ProductDetails productDetails
                = OrderResponse.ProductDetails
                .builder()
                .productName(productResponse.getName())
                .quantity(order.getQuantity())
                .price(productResponse.getPrice())
                .productId(productResponse.getProductId())
                .build();


        OrderResponse.PaymentDetails paymentDetails
                = OrderResponse.PaymentDetails
                .builder()
                .paymentId(paymentResponse.getPaymentId())
                .paymentStatus(paymentResponse.getStatus())
                .paymentDate(paymentResponse.getPaymentDate())
                .paymentMode(paymentResponse.getPaymentMode())
                .build();

        OrderResponse orderResponse=OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .productDetails(productDetails)
                .paymentDetails(paymentDetails)
                .userDetails(userDetails)
                .build();


        return orderResponse;
    }
}
