package com.example.utils;

import com.example.pojo.Content;
import com.sun.tools.jconsole.JConsoleContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


@Component
public class HtmlParseUtil {
//    public static void main(String[] args) throws Exception {
//        new HtmlParseUtil().parseJD("心理学").forEach(System.out::println);
//    }


    public List<Content> parseJD(String keywords) throws Exception {
        //获取请求 https://search.jd.com/Search?keyword=java
        // 前提，需要联网，ajax不能获取到！模拟浏览器
        String url = "https://search.jd.com/Search?keyword=" + keywords;
        //解析网页 (Jsoup返回Document就是浏览器Document对象)

        Document document = Jsoup.parse(new URL(url), 30000);

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

                Content content = new Content();
                content.setImg(img);
                content.setPrice(price);
                content.setTitle(title);
                goodsList.add(content);
            }


        }
        System.out.println(goodsList.size());
        return goodsList;
    }

}
