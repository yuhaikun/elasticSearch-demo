package com.example.utils;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentConverter;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.StreamOpenOfficeDocumentConverter;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class FileConvertUtil {


//     默认转换后文件后缀
    private static final String DEFAULT_SUFFIX = "pdf";

//    openoffice_port
    private static final Integer OPENOFFICE_PORT = 8100;

    /**
     * 方法描述office文档转换为PDF（处理本地文件）
     * @param sourcePath
     * @param suffix
     * @return
     * @throws Exception
     */
    public static InputStream convertLocalFile(String sourcePath,String suffix) throws Exception {
        File inputFile = new File(sourcePath);
        FileInputStream inputStream = new FileInputStream(inputFile);
        return convertCommonByStream(inputStream,suffix);
    }

    /**
     * 将网上的office文档转换为pdf
     * @param netFileUrl
     * @param suffix
     * @return
     * @throws Exception
     */
    public static InputStream convertNetFile(String netFileUrl,String suffix) throws Exception {
//        创建URL
        URL url = new URL(netFileUrl);
//        试图连接并取得返回状态码
        URLConnection urlConnection = url.openConnection();
        urlConnection.connect();
        HttpURLConnection httpconn = (HttpURLConnection) urlConnection;
        int httpResult = httpconn.getResponseCode();
        if (httpResult == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = urlConnection.getInputStream();
            return convertCommonByStream(inputStream,suffix);
        }
        return null;
    }

    /**
     * 将文件以流的形式转换
     * @param inputStream
     * @param suffix
     * @return
     * @throws Exception
     */
    public static InputStream convertCommonByStream(InputStream inputStream,String suffix) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        连接openoffice端口号，然后进行连接
        OpenOfficeConnection connection = new SocketOpenOfficeConnection(OPENOFFICE_PORT);
        connection.connect();
//        将传输连接传到转换器中
        DocumentConverter converter = new StreamOpenOfficeDocumentConverter(connection);
//         进行文件流的转换，将输入流的格式转换成输出流的格式
        DefaultDocumentFormatRegistry formatReg = new DefaultDocumentFormatRegistry();
        DocumentFormat targetFormat = formatReg.getFormatByFileExtension(DEFAULT_SUFFIX);
        DocumentFormat sourceFormat = formatReg.getFormatByFileExtension(suffix);
        converter.convert(inputStream,sourceFormat,out,targetFormat);
//        连接断开
        connection.disconnect();
        return outputStreamConvertInputStream(out);
    }

    /**
     * 输出流转换成输入流
     * @param out
     * @return
     * @throws Exception
     */
    public static ByteArrayInputStream outputStreamConvertInputStream(final OutputStream out) throws Exception {
        ByteArrayOutputStream baos = (ByteArrayOutputStream) out;
        return new ByteArrayInputStream(baos.toByteArray());
    }

    public static void main(String[] args) throws Exception {
        InputStream stream = convertLocalFile("/Users/yuhaikun/Downloads/test", "docx");
    }
}
