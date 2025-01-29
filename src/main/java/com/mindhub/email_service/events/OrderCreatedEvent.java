package com.mindhub.email_service.events;

import java.util.ArrayList;
import java.util.List;

public class OrderCreatedEvent {
    private Long orderId;
    private String customerEmail;
    private List<ProductDTO> products = new ArrayList<>();

    public OrderCreatedEvent() {
    }

    public OrderCreatedEvent(Long orderId, String customerEmail, List<ProductDTO> products) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.products = products;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public void setProducts(List<ProductDTO> products) {
        this.products = products;
    }
}

