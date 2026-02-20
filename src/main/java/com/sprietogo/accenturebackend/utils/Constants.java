package com.sprietogo.accenturebackend.utils;

import org.springframework.stereotype.Component;

@Component
public class Constants {

    private Constants() {
    }

    public static final String ERROR_NOT_BLANK = "No puede ser un valor vacio";
    public static final String ERROR_NOT_NULL = "No puede ser un valor nulo";


    public static final String BRANCH_PRODUCT_NOT_FOUND = "BRANCH_PRODUCT_NOT_FOUND";
    public static final String BRANCH_REQUIRED ="BRANCH_ID_REQUIRED";
    public static final String BRANCH_ID_MSG_REQUIRED ="branchId is required";
    public static final String PRODUCT_REQUIRED = "PRODUCT_ID_REQUIRED";
    public static final String PRODUCT_MSG_REQUIRED = "productId is required";
    public static final String STOCK_REQUIRED = "STOCK_INVALID";
    public static final String STOCK_MSG_REQUIRED = "stock must be >= 0";
    public static final String BRANCH_NOT_FOUND = "BRANCH_NOT_FOUND";
    public static final String BRANCH_MSG_REQUIRED ="Branch not found: ";
    public static final String PRODUCT_NOT_FOUND = "PRODUCT_NOT_FOUND";
    public static final String PRODUCT_NOT_FOUND_MSG = "Product not found: " ;

}
