package com.example.ordersystem.product.exception;

import com.example.ordersystem.global.error.ErrorCode;
import com.example.ordersystem.global.error.exception.BusinessException;

public class NotEnoughStockException extends BusinessException {

    public NotEnoughStockException(String message) {
        super(message, ErrorCode.INVALID_INPUT_VALUE);
    }
}
