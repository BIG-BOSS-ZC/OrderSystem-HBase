package com.hopu.bigdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    private int id;
    private int parentid;
    private String url;
    private String name;
    private int flag;

}
