package com.hopu.bigdata.home;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GetJson {
    public static void jsonTest2() {
        String url = "https://fe-api.zhaopin.com/c/i/sou?pageSize=90&cityId=736&workExperience=-1&education=-1&companyType=-1&employmentType=-1&jobWelfareTag=-1&kw=Java%E5%BC%80%E5%8F%91&kt=3&_v=0.93944684&x-zp-page-request-id=8fea228e31914017911c7ab54b43a6fe-1569291259715-900792&x-zp-client-id=9737d74b-8ed1-422e-b4dd-a0049c966489";
        String jsonStr = CrawlerUtil.getSource(url);
        JSONObject jsonObject = new JSONObject(jsonStr);
        System.out.println(jsonObject.get("code"));

        JSONArray jsonArray = (JSONArray) ((JSONObject) jsonObject.get("data")).get("results");
        System.out.println(((JSONObject) jsonArray.get(0)).get("jobName"));
    }


    public static void jsonTest() {
        String url = "http://s.cjol.com/service/joblistjson.aspx?&ListType=2&page=2";
        String jsonStr = CrawlerUtil.getSource(url);
        // 可以把jsonObject理解为Map，使用get方法，获取属性值。
        JSONObject jsonObject = new JSONObject(jsonStr);
        System.out.println(jsonObject.get("JobListHtml"));
    }

    public static void getJobs(String url){
//        http://s.cjol.com/service/joblistjson.aspx?&ListType=2&page=2
        String jsonStr = CrawlerUtil.getSource(url);
        // 可以把jsonObject理解为Map，使用get方法，获取属性值。
        JSONObject jsonObject = new JSONObject(jsonStr);
        Object jobListHtml = jsonObject.get("JobListHtml");
        String html=jobListHtml.toString();
        Document doc = Jsoup.parse(html);
        Elements li = doc.select("li");
        int i=1;
        String txt="";
        for (Element l:li){
            txt+=l.text();
            if(i%8==0){
                txt+="\n";
            }else {
                txt+="\t";
            }
            i++;
        }
        try {
            writeTxt(txt,"D://jobs.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTxt(String str,String path) throws IOException {
        File txt=new File(path);
        if(!txt.exists()){
            txt.createNewFile();
        }
        byte bytes[]=new byte[512];
        bytes=str.getBytes();
        int b=bytes.length;   //是字节的长度，不是字符串的长度
        FileOutputStream fos=new FileOutputStream(path,true);
        fos.write(bytes,0,b);
        fos.close();
    }


    /*public static void main(String[] args) {
        for (int i=1;i<999;i++){
            getJobs("http://s.cjol.com/service/joblistjson.aspx?&ListType=2&page="+i+"\"");
        }

    }*/

}
