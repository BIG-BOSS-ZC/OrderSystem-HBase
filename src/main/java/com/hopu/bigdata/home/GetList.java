package com.hopu.bigdata.home;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hopu.bigdata.mapper.CategoryMapper;
import com.hopu.bigdata.mapper.ProductMapper;
import com.hopu.bigdata.model.Category;
import com.hopu.bigdata.model.Product;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GetList implements CommandLineRunner {
    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    List<Category> categories;


    public void getCategorys(){
        QueryWrapper queryWrapper1=new QueryWrapper();
        queryWrapper1.eq("flag",1);
//        queryWrapper1.between(false,"id",0,500);
        categories = categoryMapper.selectList(queryWrapper1);
    }

    public void getPages(String categoryUrl,int cateid){
        int page=0;
        try {

            Document doc = Jsoup.connect(categoryUrl).header("user-agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                    .get();
            Elements select = doc.select(".fp-text i");
            for (Element e:select){
                page = Integer.parseInt(e.text());
            }
            if(page>50){
                page=50;
            }

            for(int i=1;i<=page;i++){
                getProduct(categoryUrl+"&page="+i,cateid);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getProduct(String categoryUrl,int cateid){
        try {

//            List<Product> plist=new ArrayList<Product>();
            Document doc = Jsoup.connect(categoryUrl).header("user-agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                    .get();
            Elements items = doc.select(".j-sku-item");
            String price0="";
            String pid="";

            Elements pnames = doc.select(".p-name em");

            Elements imgs = doc.select(".gl-item img");
            for(Element e:items){
                //获取proid  shopid
                price0+="J_"+e.attr("data-sku")+",";
                pid+=e.attr("data-sku")+",";
            }
            String price1="https://p.3.cn/prices/mgets?skuIds="+price0;
            String js= CrawlerUtil.getSource(price1,"gbk");
//            System.out.println(js);
            JSONArray ja=new JSONArray(js);

            String shopurl="https://chat1.jd.com/api/checkChat?pidList="+pid;

            String shopinfo = CrawlerUtil.getSource(shopurl, "utf8", "header");
            String reg="(\\[.*?\\])";
            Pattern pattern=Pattern.compile(reg);
            Matcher matcher=pattern.matcher(shopinfo);
            String shopjs="[]";
            if(matcher.find()){
                shopjs=matcher.group(1);
//                System.out.println(shopjs);
            }
            JSONArray shop=new JSONArray(shopjs);



            for (int i = 0; i <items.size() ; i++) {
                Element item = items.get(i);
                Element pname = pnames.get(i);
                Element img = imgs.get(i);
                price0 += "J_" + item.attr("data-sku");
                JSONObject jo = (JSONObject) ja.get(i);
                String price = jo.get("op").toString();

                JSONObject jo1 = (JSONObject) shop.get(i);
                String shop1 = jo1.get("seller").toString();


                Product p = new Product();
                p.setProid(item.attr("data-sku").toString());
                p.setName(pname.text());


                String img1 = img.attr("src").toString();
                if (img1.equals("")) {
                    img1 = img.attr("data-lazy-img").toString();
                }

                p.setImgurl(img1);
                p.setCategoryid(cateid + "");
                p.setPrice(price);
                p.setShopid(item.attr("jdzy_shop_id").toString());
                p.setShopname(shop1);
                productMapper.insert(p);

            }

            /*for (Product p:plist){
                System.out.println(p.toString());
            }*/
        } catch (IOException e) {
            e.printStackTrace();
        }catch (Exception e){
//            e.printStackTrace();
        }
    }

    public void startThreads(){
        //获取所有类别
        getCategorys();
        List<Category> categories1=new ArrayList<>();
        List<Category> categories2=new ArrayList<>();
        List<Category> categories3=new ArrayList<>();
        List<Category> categories4=new ArrayList<>();
        for (int i=0;i<categories.size();i++){
            if(i<400){
                categories1.add(categories.get(i));
            }else if(i<800){
                categories2.add(categories.get(i));
            }else if(i<1200){
                categories3.add(categories.get(i));
            }else {
                categories4.add(categories.get(i));
            }
        }


        System.out.println(categories1.size()+","+categories2.size()+","+categories3.size()+","+categories4.size()+",");

        //四线程
        new Thread(() -> {
            System.out.println("thread1 start");
            try {
                for (int i = 399; i < categories1.size(); i++) {
                    System.out.println("Thread1 index -->"+i);
                    Category c=categories1.get(i);
                    String url=c.getUrl();
                    int id=c.getId();
                    getPages(url,id);
                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("thread1 finish");
        }
        ).start();

        new Thread(() -> {
            System.out.println("thread2 start");
            try {
                for (int i = 399; i < categories2.size(); i++) {
                    System.out.println("Thread2 index -->"+i);
                    Category c=categories2.get(i);
                    String url=c.getUrl();
                    int id=c.getId();
                    getPages(url,id);

                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("thread2 finish");
        }
        ).start();

        new Thread(() -> {
            System.out.println("thread3 start");
            try {
                for (int i = 98; i < categories3.size(); i++) {
                    System.out.println("Thread3 index -->"+i);
                    Category c=categories3.get(i);
                    String url=c.getUrl();
                    int id=c.getId();
                    getPages(url,id);

                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("thread3 finish");
        }
        ).start();

        new Thread(() -> {
            System.out.println("thread4 start");
            try {
                for (int i = 147; i < categories4.size(); i++) {

                    System.out.println("Thread4 index -->"+i);
                    Category c=categories4.get(i);
                    String url=c.getUrl();
                    int id=c.getId();
                    getPages(url,id);

                    Thread.sleep(5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("thread4 finish");
        }
        ).start();
    }


    @Override
    public void run(String... args) throws Exception {
//        startThreads();
    }
}
