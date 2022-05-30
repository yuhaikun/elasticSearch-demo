package com.artofsolving.jodconverter;



import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.DocumentFormatRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * 新建com.artofsolving.jodconverter重写BasicDocumentFormatRegistry类
 * 解决docx、xlsx、pptx三种格式文件无法被打开的问题
 */
public class BasicDocumentFormatRegistry implements DocumentFormatRegistry {

    private List documentFormats = new ArrayList();

    public void addDocumentFormat(DocumentFormat documentFormat) {
        documentFormats.add(documentFormat);
    }

    protected List getDocumentFormats() {
        return documentFormats;
    }


    /**
     * 将后缀名进行扩展,以支持对docx、pptx、xlsx格式对支持
     * @param s
     * @return
     */
    @Override
    public DocumentFormat getFormatByFileExtension(String s) {
        if (s == null) {
            return null;
        }
//        将文件名后缀统一转化
        if (s.indexOf("doc") >= 0) {
            s = "doc";
        }
        if (s.indexOf("ppt") >= 0) {
            s = "ppt";
        }
        if (s.indexOf("xls") >= 0) {
            s = "xls";
        }
        String lowerExtension = s.toLowerCase();
        for (Iterator it = documentFormats.iterator(); it.hasNext();) {
            DocumentFormat format = (DocumentFormat) it.next();
            if (format.getFileExtension().equals(lowerExtension)) {
                return format;
            }
        }
        return null;
    }

    @Override
    public DocumentFormat getFormatByMimeType(String s) {

        for (Iterator it = documentFormats.iterator();it.hasNext();) {
            DocumentFormat format = (DocumentFormat) it.next();
            if (format.getMimeType().equals(s)) {
                return format;
            }

        }

        return null;
    }
}

