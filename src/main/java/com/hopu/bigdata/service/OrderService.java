package com.hopu.bigdata.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hopu.bigdata.hbaseapi.HBaseApi;
import com.hopu.bigdata.home.BillionOrder;
import com.hopu.bigdata.mapper.ProductMapper;
import com.hopu.bigdata.mapper.UserinfoMapper;
import com.hopu.bigdata.model.Order;
import com.hopu.bigdata.model.Product;
import com.hopu.bigdata.model.Userinfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Component
@Service
public class OrderService implements CommandLineRunner {

    @Autowired
    ProductMapper productMapper;
    @Autowired
    UserinfoMapper userinfoMapper;


    //该用户的订单
    public Map<String, Map<String, String>> userOrder;

    //Map<String, Map<String, String>>
    //Map<rowkey , Map < 列 ，值>>

    /**
     * 通过userid在hbase查询该用户的所有订单
     * @param userid 用户id
     * @return userOrder 该用户的所有订单，类型是Map<String, Map<String, String>>
     */
    public Map<String, Map<String, String>> selectOrdersByUser(int userid){
        //scan 'order1',{STARTROW=>userid,ENDROW=>userid+1}
        userOrder = HBaseApi.scanByStartEndRowkey("order1", BillionOrder.changeNum(userid,7),BillionOrder.changeNum(userid+1,7) );
        System.out.println(userOrder.toString());
        return userOrder;
    }

    /**
     * 用订单号查询订单信息
     * 订单号-->索引表-->主表rowkey-->订单信息
     * @param orderId
     * @return orderdata
     */
    public Map<String, String> selectOrderByOrderId(String orderId) {
        Map<String, String> orderdata=new HashMap<>();
        try {
            //查询索引表，得到rowkey
            System.out.println("-=-=--=-=-=orderid"+orderId);
            Map<String, String> orderindex = HBaseApi.getDataByRowKey("orderindex", orderId);
            String rowkey = orderindex.get("rowkey");
            System.out.println("-=-=--=-=-="+rowkey);
            //通过rowkey查询订单信息
            orderdata = HBaseApi.getDataByRowKey("order1", rowkey);
//            System.out.println(orderdata.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return orderdata;
    }

    /**
     * 在该用户的  一个  订单里，所有的商品信息
     * @param orderinfo
     * @return List<Product> productList
     */
    public List<Product> orderProduct(Map<String, String> orderinfo){
        List<Product> productList=new ArrayList<>();
            String proids = orderinfo.get("proids");//[1,2,3]
            String proids2 = proids.substring(1, proids.length() - 1);
            String[] proids3 = proids2.split(",");

            for(String proid:proids3){
//                System.out.println(proid.trim());

                QueryWrapper queryWrapper=new QueryWrapper();
                queryWrapper.eq("proid",proid.trim());
                List<Product> products = productMapper.selectList(queryWrapper);

//                System.out.println(product.getImgurl());
                for(Product p:products){
//                    System.out.println(p.toString());
                    productList.add(p);
                }

            }

        return productList;
    }
    /**
     * 在该用户的  所有  订单里，所有的商品信息
     * @param userOrder
     * @return List<Product> productList
     */
    public List<Product> orderProduct2(Map<String,Map<String, String>> userOrder){
        Set<Map.Entry<String, Map<String, String>>> entries = userOrder.entrySet();
        List<Product> productList=new ArrayList<>();
        for(Map.Entry e:entries){
            String rowkey=e.getKey().toString();
            Map<String, String> orderinfo =(Map) e.getValue();
            String proids = orderinfo.get("proids");//[1,2,3]
            String proids2 = proids.substring(1, proids.length() - 1);
            String[] proids3 = proids2.split(",");

            for(String proid:proids3){
                System.out.println(proid.trim());

                QueryWrapper queryWrapper=new QueryWrapper();
                queryWrapper.eq("proid",proid.trim());
                List<Product> products = productMapper.selectList(queryWrapper);

//                System.out.println(product.getImgurl());
                for(Product p:products){
                    System.out.println(p.toString());
                    productList.add(p);
                }

            }
        }
        return productList;
    }

    @Override
    public void run(String... args) throws Exception {
//        selectOrderByOrderId("100000");
//        selectOrdersByUser(new Userinfo(2,"","","",34,"","","",""));
    }
}
