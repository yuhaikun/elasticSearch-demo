package com.example.service;


import com.alibaba.fastjson.JSON;
import com.example.pojo.Content;
import com.example.utils.ESconsts;
import com.example.utils.FileConvertUtil;
import com.example.utils.FormatUtil;
import com.example.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//业务编写
@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

//    //不能直接使用 @Autowired 需要spring容器
//    public static void main(String[] args) throws Exception {
//        new ContentService().parseContent("java");
//    }

    //1、解析数据放入es索引中
    public Boolean parseContent(String keywords) throws Exception {
        List<Content> contents = new HtmlParseUtil().parseJD(keywords);
        //把查询到的数据放入es中
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");

        for (int i = 0; i < contents.size(); i++) {
//            .id(""+(i+1))
            bulkRequest.add(new IndexRequest("jd_goods")
                    .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
//        System.out.println("打印的数量为："+contents.size());
        return !bulk.hasFailures();
    }

    //2. 获取这些数据实现搜索功能
    public List<Map<String,Object>> searchPage(String keyword,int pageNo,int pageSize) throws IOException {
        if (pageNo <= 1) {
            pageNo = 1;
        }
        // 条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        // 精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            list.add(documentFields.getSourceAsMap());
        }

        return list;
    }


    //获取这些数据实现搜索高亮功能
    public List<Map<String,Object>> searchPageHighlightBuilder(String keyword,int pageNo,int pageSize) throws Exception {
        if (pageNo <= 1) {
            pageNo = 1;
        }
        // 条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        // 精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false); //多个高亮显示关闭
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);


        //解析结果
        ArrayList<Map<String,Object>> list = new ArrayList<>();
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            //解析高亮的字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();//原来的结果
            if (title!=null) {
                Text[] fragments = title.fragments();
                String n_title = "";
                for (Text text : fragments) {
                    n_title += text;
                }
                sourceAsMap.put("title", n_title);
            }

            list.add(sourceAsMap);
        }
//        System.out.println(list);
//       这里判断数据是否为空，如果为空，则重新进行关键词载入
        if (list.size() == 0) {
//            System.out.println("11111");
            parseContent(keyword);
//            System.out.println("2222");
            return searchPageHighlightBuilder(keyword, pageNo, pageSize);
        }else {
            return list;
        }

    }

    /**
     * @Description 系统文件在线预览接口
     * @param url
     * @param response
     * @throws Exception
     */
    public OutputStream onlinePreview(String url, HttpServletResponse response) throws Exception {
//        获取文件类型
        String[] str = url.split("\\.");

        System.out.println(str[0]);
        if (str.length == 0) {
            throw new Exception("文件格式不支持预览");
        }
        String suffix = str[str.length-1];
//        函数 数组
//        if (!suffix.equals("txt")&&!suffix.equals("doc")&&!suffix.equals("docx")&&!suffix.equals("xls")
//            && !suffix.equals("xlsx")&& !suffix.equals("ppt")&& !suffix.equals("pptx")){
//            System.out.println("后缀为："+suffix);
//            throw new Exception("文件格式不支持预览");
//        }

        if (FormatUtil.judgeFormat(suffix) == false) {
            throw new Exception("文件格式不支持预览");
        }


        InputStream in = FileConvertUtil.convertLocalFile(url, suffix);
        OutputStream outputStream = response.getOutputStream();
//        创建存放文件内容的数组
        byte[] buff = new byte[ESconsts.BYTEARRAY_LENGTH];
//        所读取的内容使用n来接收
        int n;
//        当没有读取完，继续读取，循环
        while((n=in.read(buff))!=-1){
//            将字节数组的数据全部写入到输出流
            outputStream.write(buff,0,n);
        }
//        强制将缓存区的数据进行输出
        outputStream.flush();
//        关流
        outputStream.close();
        in.close();
        return outputStream;
    }

    public HttpServletResponse download(String path,HttpServletResponse response){
        try {
//        path是指想下载文件的路径
            File file = new File(path);
//        取得文件名
            String filename = file.getName();
//        取得文件的后缀名
            String ext = filename.substring(filename.lastIndexOf(".")+1).toUpperCase();
//        以流的形式下载文件
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(path));
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();
//        清空response
            response.reset();
//        设置response的header
            response.addHeader("Content-Disposition","attachment;filename="+new String(filename.getBytes()));
            response.addHeader("Content-Length",""+file.length());
            OutputStream toClient = new BufferedOutputStream(response.getOutputStream());
            response.setContentType("application/octet-stream");
            toClient.write(buffer);
            toClient.flush();
            toClient.close();
//            return toClient;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
