package com.hopu.bigdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    //订单id，用户id，商品id集合，订单时间，订单完成时间，订单状态
    private String orderid;
    private String userid;
    private List<String> proids;
    private String ordertime;
    private String state;

}
