package com.hopu.bigdata.home;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hopu.bigdata.mapper.ProductMapper;
import com.hopu.bigdata.mapper.UserinfoMapper;
import com.hopu.bigdata.model.Order;
import com.hopu.bigdata.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class BillionOrder implements CommandLineRunner {
    /*
    * 1.取一百万用户
    * 2.每个用户生成100个订单
    * 3.每个订单10个商品
    *
    * */
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    // order类别队列
    private static final BlockingQueue<Order> queueOrder = new ArrayBlockingQueue<Order>(600);




    @Autowired
    UserinfoMapper userinfoMapper;
    @Autowired
    ProductMapper productMapper;

    List<Product> products;

    /*public Userinfo getUser(int id){

        QueryWrapper<Userinfo> queryWrapper=new QueryWrapper<>();
        Userinfo userinfo = userinfoMapper.selectById(id);
        return userinfo;
    }*/

    public void getProducts(int start){
        products=null;
        QueryWrapper<Product> queryWrapper=new QueryWrapper<>();
        queryWrapper.between("sortid",start,start+10);
        //从"start"开始。查询"10"条
        products = productMapper.selectList(queryWrapper);
    }

    public String getTime(){
        //2010/01/01-2019/10/30
        Integer offset=(int) (310132800*Math.random());
        return (1262275200+offset)+"";
    }


    public void buildOrder(){
        System.out.println("start");
        List<Order> olist=new ArrayList<>();
        int temp=0;
        int userid=0;
        int proindex=0;
        String state="";
        //100000001
        //改为10000000
        for (int i=1;i<=100000001;i++){
            List<Product> products=new ArrayList<>();
            List<String> proids=new ArrayList<>();
            temp++;
            if(temp==1) {
                userid++;
            }
            if(userid>1000001){
                System.out.println("hbase insert finished!");
                break;
            }

            if(proindex*10<= 1026900){
                getProducts(proindex*10);
                proindex++;
            }else {
                proindex=0;
            }


            Order order=new Order();

            order.setOrderid(changeNum(i,9));

            order.setUserid(changeNum(userid,7));

            if(temp==100){
                temp=0;
            }
            for(Product p:products){
                proids.add(p.getProid());
            }

            order.setProids(proids);
            order.setOrdertime(getTime());

            int n=(int) (1+Math.random()*100);

            if(i%n==0){
                state="canceled";
            }else {
                state="finished";
            }
            order.setState(state);
            try {
                queueOrder.put(order);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("over");
    }
    public static String changeNum(int userid,int length){
        String uid=userid+"";
        StringBuffer b=new StringBuffer();
        if(uid.length()<length){
            for (int j=0;j<length-uid.length();j++){
                b.append('0');
            }
            b.append(uid);
        }else {
            b.append(uid);
        }
        return b.toString();
    }


    @Override
    public void run(String... args) throws Exception {

        //一个线程生产order到阻塞队列，6个线程写order数据到HBASE

        /*new Thread(()->{
            System.out.println("buildOrder thread start");
            buildOrder();
        }).start();

        for (int i = 0; i < 10; i++) {
            executorService.execute(()->{
                System.out.println("insert to hbase : Thread Start");
                while (true){
                    try {
                        HBaseApi.insertData("order1",queueOrder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }*/

//        HBaseApi.getDataByRowKey("orderindex","1999999");

    }
}
