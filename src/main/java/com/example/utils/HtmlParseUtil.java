package com.example.utils;

import com.example.pojo.Content;
import com.sun.tools.jconsole.JConsoleContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.text.html.parser.Entity;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;


/**
 * 改进方法：将原有的jsoup获取数据的方法转化成jsoup+httpclient(不解析JS、Ajax，所以较快)
 * 而HtmlUnit（解析JS、Ajax，速度没有jsoup+httpclient快）
 * httpclient可以用get和post方法访问网页并返回相应内容，
 * 然后在jsoup上对返回的内容作处理再显示在客户端上可以达到预期的效果。
 */

@Component
public class HtmlParseUtil {
//    public static void main(String[] args) throws Exception {
//        new HtmlParseUtil().parseJD("心理学").forEach(System.out::println);
//    }


    public List<Content> parseJD(String keywords) throws Exception {

        //创建httpclient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建httpget实例
        HttpGet httpGet = new HttpGet(ESconsts.ES_URL + keywords);
        //模拟浏览器
        httpGet.setHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
        httpGet.setHeader("Connection","keep-alive");
        //使用代理ip
        //HttpHost proxy = new HttpHost("118.114.77.47",8080);
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(10000)
                .setSocketTimeout(1000)
                .setRedirectsEnabled(false)
                .build();
        httpGet.setConfig(config);
        //执行get请求
        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        //获取返回实体
        String content = EntityUtils.toString(entity, "utf-8");

//        单纯使用jsoup的方法
//        //获取请求 https://search.jd.com/Search?keyword=java
//        // 前提，需要联网，ajax不能获取到！模拟浏览器
//        String url = "https://search.jd.com/Search?keyword=" + keywords;
//        //解析网页 (Jsoup返回Document就是浏览器Document对象)
//
//        Document document = Jsoup.parse(new URL(url), 30000);


        Document document = Jsoup.parse(content);


        //所有在js中可以使用到方法这里都能用
        Element element = document.getElementById("J_goodsList");



        //获取所有的li元素
        Elements elements = element.getElementsByTag("li");


        ArrayList<Content> goodsList = new ArrayList<>();

        //获取元素中的内容，这里el就是每一个li标签
        for (Element el : elements) {

            if (el.attr("class").equalsIgnoreCase("gl-item")) {

            String img = el.getElementsByTag("img").eq(0).attr("data-lazy-img");
            String price = el.getElementsByClass("p-price").eq(0).text();

            String title = el.getElementsByClass("p-name").eq(0).text();

                Content contents = new Content();
                contents.setImg(img);
                contents.setPrice(price);
                contents.setTitle(title);
                goodsList.add(contents);
            }


        }
        System.out.println(goodsList.size());
        response.close();
        httpClient.close();
        return goodsList;
    }


}
