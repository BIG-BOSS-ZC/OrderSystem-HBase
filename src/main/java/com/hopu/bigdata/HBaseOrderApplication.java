package com.hopu.bigdata;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hopu.bigdata.mapper")
public class HBaseOrderApplication {

	public static final boolean jdCatFlag = false;
	public static final boolean jdItemFlag = false;
	public static final boolean userMock = false;
	public static final boolean orderMock = false;

	public static void main(String[] args) {
		SpringApplication.run(HBaseOrderApplication.class, args);
	}

}
