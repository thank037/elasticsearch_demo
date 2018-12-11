package com.thank.elasticsearch.util;

import org.apache.poi.hwpf.extractor.WordExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * description: none
 *
 * @author xiefayang
 * 2018/11/26 10:46
 */
public class POIParserUtil {

    public static WordExtractor parseDoc(String filePath) throws IOException {
        File file = new File(filePath);
        String fileName = file.getName();
        FileInputStream inputStream = new FileInputStream(file);
        System.out.println(fileName);

//        StringBuilder sb = new StringBuilder();
        WordExtractor wordExtractor = new WordExtractor(inputStream);//使用HWPF组件中WordExtractor类从Word文档中提取文本或段落

        if (!fileName.endsWith(".doc")) {
            return null;
        }
//        int i=1;
//        for(String words : wordExtractor.getParagraphText()){//获取段落内容
//            sb.append(words);
//                System.out.println(words);
//                wordMap.put("DOC文档，第（"+i+"）段内容",words);
//            i++;
//        }
        inputStream.close();

        return wordExtractor;
    }


    public static String parseDocx(String filePath) {
        //        if(fileName.endsWith(".docx")){
//            File uFile = new File("tempFile.docx");//创建一个临时文件
//            if(!uFile.exists()){
//                uFile.createNewFile();
//            }
//
//            FileCopyUtils.copy(file.getBytes(), uFile);//复制文件内容
//            OPCPackage opcPackage = POIXMLDocument.openPackage("tempFile.docx");//包含所有POI OOXML文档类的通用功能，打开一个文件包。
//            XWPFDocument document = new XWPFDocument(opcPackage);//使用XWPF组件XWPFDocument类获取文档内容
//            List<XWPFParagraph> paras = document.getParagraphs();
//            int i=1;
//            for(XWPFParagraph paragraph : paras){
//                String words = paragraph.getText();
//                System.out.println(words);
//                wordMap.put("DOCX文档，第（"+i+"）段内容",words);
//                i++;
//            }
//            uFile.delete();
//        }
        return null;
    }
}
