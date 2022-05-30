package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.example.pojo.Content;
import com.example.service.ContentService;
import com.example.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/parse/{keyword}")
    public Boolean parse(@PathVariable("keyword") String keyword) throws Exception {
//        因为es中的term查询无法识别大写字母，所以这里将大写字母转换成为小写字母
        String keywords = keyword.toLowerCase();
        return contentService.parseContent(keywords);
    }

    @GetMapping("/search/{keyword}/{pageNo}/{pageSize}")
    public List<Map<String,Object>> search(@PathVariable("keyword") String keyword,@PathVariable("pageNo") int pageNo,@PathVariable("pageSize") int pageSize) throws Exception {
        String keywords = keyword.toLowerCase();
        return contentService.searchPageHighlightBuilder(keywords, pageNo, pageSize);


    }


    @GetMapping("/api/file/onlinePreview")
    public OutputStream onlinePreview(@RequestParam("url") String url, HttpServletResponse response) throws Exception{

        return contentService.onlinePreview(url,response);
    }

    @GetMapping("/api/file/download")
    public HttpServletResponse downloadFile(@RequestParam("url") String url, HttpServletResponse response) {
        return contentService.download(url,response);
    }

}
