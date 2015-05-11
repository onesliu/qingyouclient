package com.qingyou.qingyouclient;

import com.qingyou.businesslogic.OrderList;
import com.qingyou.businesslogic.OrderStatus;
import com.qingyou.businesslogic.ProductImageList;

interface IOrderList{
    OrderList getOrderList();
    OrderStatus getOrderStatus();
    ProductImageList getProductImageList();
}