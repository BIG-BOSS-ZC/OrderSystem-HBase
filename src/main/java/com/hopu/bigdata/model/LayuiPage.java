package com.hopu.bigdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LayuiPage {
    private int code = 0;
    private String msg = "";
    private int count;
    private Collection<?> data;
}
