package com.hopu.bigdata.controller;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.hopu.bigdata.home.BillionOrder;
import com.hopu.bigdata.model.*;
import com.hopu.bigdata.service.OrderService;
import com.hopu.bigdata.util.TimeInterEnum;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

import static com.hopu.bigdata.util.TimeInterEnum.ALL;

@Controller
@RequestMapping("/user/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    private Map<String, Map<String, String>> userOrder;
    private List<Product> itemList;

    @RequestMapping("")
    public String order(HttpSession session,
                        Model model,
                        @RequestParam(required = false) String orderId) {
        if (orderId == null){
            //（orderid==null说明用户首次登陆查看订单）查询该用户所有订单信息
            userOrder=orderService.selectOrdersByUser((int) session.getAttribute("userid"));
            session.setAttribute("userOrder",userOrder);
            return "user/order-list";
        }

        System.out.println(orderId);

        Map<String, Object> result = new HashMap<>();
        //根据orderid查询order信息
        Map<String, String> orderinfo = orderService.selectOrderByOrderId(orderId);
        //取出order信息的商品
        itemList = orderService.orderProduct(orderinfo);

        session.setAttribute("itemList",itemList);

        result.put("orderId", orderinfo.get("orderid") );
        result.put("state", orderinfo.get("state"));
        long timeStamp = Long.parseLong(orderinfo.get("ordertime"))*1000;  //获取时间戳(一定是long型的数据)
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
        String ordertime = sdf.format(new Date(timeStamp));   // 时间戳转换成时间
        result.put("orderTime", ordertime);
        result.put("itemList", itemList);
        model.addAttribute("order", result);

        return "user/order";
    }

    @ResponseBody
    @RequestMapping("list")
    public LayuiPage orderList(HttpSession session,
                               Model model,
               @RequestParam(required = false, defaultValue = "ALL") String timeInterval,
               @RequestParam(required = false) int page,
               @RequestParam(required = false) int limit
    ) throws IOException {



        List<Product> productList;
        List<OrderH> orderHList=new ArrayList<>();
        System.out.println("--------++++++++++++"+userOrder.toString());

        if(session.getAttribute("productList")==null){
            //==null说明首次进入list页，需要查询
            productList = orderService.orderProduct2(userOrder);
            session.setAttribute("productList",productList);
        }else {
            productList=(List<Product>) session.getAttribute("productList");
        }

        System.out.println(timeInterval);

        switch (TimeInterEnum.valueOf(timeInterval)){
            case LAST_WEEK:
                long time=(long) (System.currentTimeMillis()/1000 - 86400*7)*1000;//2019-10-26
                return selectTimeOrder(productList,orderHList,time,page,limit);
            case ALL:
                long time2=0L;
                return selectTimeOrder(productList,orderHList,time2,page,limit);
            case LAST_MONTH:
                long time3=(long) (System.currentTimeMillis()/1000 - 86400*30)*1000;
                return selectTimeOrder(productList,orderHList,time3,page,limit);
            case LAST_3_MONTH:
                long time4=(long) (System.currentTimeMillis()/1000 - 86400*30*3)*1000;
                return selectTimeOrder(productList,orderHList,time4,page,limit);
            case LAST_6_MONTH:
                long time5=(long) (System.currentTimeMillis()/1000 - 86400*30*6)*1000;
                return selectTimeOrder(productList,orderHList,time5,page,limit);
            case LAST_YEAR:
                long time6=(long) (System.currentTimeMillis()/1000 - 86400*365)*1000;
                return selectTimeOrder(productList,orderHList,time6,page,limit);
            case LAST_3_YEAR:
                long time7=(long) (System.currentTimeMillis()/1000 - 86400*365*3)*1000;
                return selectTimeOrder(productList,orderHList,time7,page,limit);
            default:
                System.out.println("default");
                long time8=0L;
                return selectTimeOrder(productList,orderHList,time8,page,limit);
        }
    }


    public LayuiPage selectTimeOrder(List<Product> productList,List<OrderH> orderHList,long time,int page,int limit){
        System.out.println("--------++++++++++++"+userOrder.toString());
        Set<Map.Entry<String, Map<String, String>>> entries = userOrder.entrySet();
        int count = 0;
        int index = -1;
        for(Map.Entry<String, Map<String, String>> e:entries){
            index++;
            String rowkey = e.getKey();
            Map<String, String> orderinfo = e.getValue();
            long timeStamp = Long.parseLong(orderinfo.get("ordertime"))*1000;
//            System.out.println(timeStamp);
            if(timeStamp<time) {
                continue;
            }

                //获取当前时间戳,也可以是你自已给的一个随机的或是别人给你的时间戳(一定是long型的数据)
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
                String ordertime = sdf.format(new Date(timeStamp));   // 时间戳转换成时间
//                System.out.println(ordertime);
                OrderH orderH = new OrderH(
                        orderinfo.get("orderid"),
                        orderinfo.get("userid"),
                        productList.get(index*11).getName(),
                        ordertime,
                        orderinfo.get("state")
                );
                count++;
                if (count <= page * limit && count > page * limit - limit) {
                    orderHList.add(orderH);
                }
        }

        LayuiPage layuiPage = new LayuiPage();
        layuiPage.setData(orderHList);
        layuiPage.setCount(count);
        return layuiPage;
    }

}
