package com.hopu.bigdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String proid;
    private String name;
    private String imgurl;
    private String price;
    private String categoryid;
    private String shopid;
    private String shopname;
    private int sortid;
}
