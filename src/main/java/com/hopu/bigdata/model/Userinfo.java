package com.hopu.bigdata.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Userinfo {
    private int id;
    private String loginname;
    private String password;
    private String truename;
    private int age;
    private String sex;
    private String email;
    private String address;
    private String intime;
}
