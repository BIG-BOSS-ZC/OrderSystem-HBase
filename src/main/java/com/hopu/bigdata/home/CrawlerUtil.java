package com.hopu.bigdata.home;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CrawlerUtil {

    public static String getSource(String url) {
        return getSource(url, "utf8");
    }

    public static String getSource(String url, String charset) {
        String src = "";
        try {
            URL urlObj = new URL(url);
            // 1. 建立http连接
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            // 2. 配置request header
            con.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
            // 3. 打开连接
            con.connect();

            // 判断是否请求成功
            if (con.getResponseCode() == 200) {
                //4. 请求成功则获取网页源代码
                InputStream is = con.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte [] bytes = new byte[1<<20];
                int len;

                while ((len=is.read(bytes)) != -1) {
                    baos.write(bytes, 0, len);
                }

                // 字符 = 字节 + 编码格式
                src = new String(baos.toByteArray(), charset);
                baos.close();
                is.close();

                return src;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

        return src;
    }

    public static String getSource(String url,String charset,String header) {
        String src = "";
        try {
            URL urlObj = new URL(url);
            // 1. 建立http连接
            HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
            // 2. 配置request header
            con.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
            con.setRequestProperty("Sec-Fetch-Mode","no-cors");
            con.setRequestProperty("Referer","https://list.jd.com/list.html");
            // 3. 打开连接
            con.connect();

            // 判断是否请求成功
            if (con.getResponseCode() == 200) {
                //4. 请求成功则获取网页源代码
                InputStream is = con.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte [] bytes = new byte[1<<20];
                int len;

                while ((len=is.read(bytes)) != -1) {
                    baos.write(bytes, 0, len);
                }

                // 字符 = 字节 + 编码格式
                src = new String(baos.toByteArray(), charset);
                baos.close();
                is.close();

                return src;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return src;
    }
}
