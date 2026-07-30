package com.project.ecommerce.Request;

import com.project.ecommerce.Enums.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    private OrderStatus orderStatus;

}