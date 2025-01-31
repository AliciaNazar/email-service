package com.mindhub.email_service.events;

import java.util.List;

public class OrderToPdfDTO {
    private Long orderId;
    private Long userId;
    private String userMail;
    private List<ProductDTO> newProductList;

    public OrderToPdfDTO(Long orderId, Long userId, String userMail, List<ProductDTO> newProductList) {
        this.orderId = orderId;
        this.userId = userId;
        this.userMail = userMail;
        this.newProductList = newProductList;
    }

    public OrderToPdfDTO() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUserMail() {
        return userMail;
    }

    public List<ProductDTO> getnewProductList() {
        return newProductList;
    }
}
