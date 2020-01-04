package com.hopu.bigdata.home;

import com.hopu.bigdata.mapper.CategoryMapper;
import com.hopu.bigdata.model.Category;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JdHome implements CommandLineRunner {

    @Autowired
    CategoryMapper categoryMapper;

//    private static List<Category> categoryList=new ArrayList<Category>();
    private int tempId=1;

    public void getCategory(){
        String url = "https://dc.3.cn/category/get?&callback=getCategoryCallback";
        String js= CrawlerUtil.getSource(url,"gbk");
        String jsonStr = js.substring(20,js.length()-1);
        JSONObject jsonObject = new JSONObject(jsonStr);
        JSONArray data =(JSONArray) jsonObject.get("data");
        for(int i=0;i<data.length();i++){
            JSONArray s = data.getJSONObject(i).getJSONArray("s");
            getCategoryUrl(s,-1);
        }
//        System.out.println(jsonStr);

    }

    public void getCategoryUrl(JSONArray data,int parentid){
        if(data.length()==0){
            return;
        }
        for(int i=0;i<data.length();i++){
            JSONObject d0 =(JSONObject) data.get(i);
            JSONArray s =(JSONArray) d0.get("s");
            int id=tempId++;
            int flag=0;
            String str=d0.get("n").toString();
            String[] split = str.split("\\|");
            String url=split[0];
            if(url.contains("#")){
                url=url.replace("#J_crumbsBar","");
            }
            String reg="^list.jd.com";
            Pattern pattern=Pattern.compile(reg);
            Matcher matcher=pattern.matcher(url);
            if(matcher.find()){
                url="https://"+url;
            }
        /*if(url.matches("/^list.jd.com/")){
            url="https://"+url;
        }*/
            if(url.contains("&page=")){
                url=url.replaceAll("&page=\\d","");
            }else if(url.contains("-")){
                url="https://list.jd.com/list.html?cat="+url.replace("-",",");
            }
            pattern=Pattern.compile(reg);
            matcher=pattern.matcher(url);
            if(url.startsWith("http")&&parentid!=-1&&parentid!=1){
                flag=1;
            }

            Category category=new Category(id,parentid,url,split[1],flag);
            categoryMapper.insert(category);
//                categoryMapper.insert(category);
                    //System.out.println(d0.get("n"));
                    //list.jd.com/list.html?cat=737,794,798&ev=4155_76344&sort=sort_rank_asc&trans=1&JL=2_1_0#J_crumbsBar|超薄电视||0
            getCategoryUrl(s,id);
        }

    }

    @Override
    public void run(String... args) throws Exception {
//        getCategory();
    }
}
