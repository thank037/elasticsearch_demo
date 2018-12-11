package com.thank.elasticsearch.hotword;

import com.thank.elasticsearch.util.POIParserUtil;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * description: none
 *
 * @author xiefayang
 * 2018/11/26 10:16
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestPoiParser {

    @Autowired
    private TransportClient client;

    private static final String INDEX = "hot_word_index";
    private static final String TYPE = "doc";

    @Test
    public void testParseWord() throws IOException {

        String filePath = "G:\\需求规格说明书.doc";
        WordExtractor doc = POIParserUtil.parseDoc(filePath);
        if (doc == null) {
            return;
        }
        String fileName = new File(filePath).getName();
        SummaryInformation information = doc.getSummaryInformation();
        String content = doc.getText();
        String author = information.getAuthor();
        String lastAuthor = information.getLastAuthor();
        int wordCount = information.getWordCount();
        Date createDateTime = information.getCreateDateTime();
        String applicationName = information.getApplicationName();


        XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                .field("fileName", fileName)
                .field("content", content)
                .field("author", author)
                .field("lastAuthor", lastAuthor)
                .field("wordCount", wordCount)
                .field("applicationName", applicationName)
                .field("createDateTime", createDateTime)
                .endObject();

        IndexResponse response = this.client.prepareIndex(INDEX, TYPE).setSource(builder).get();
        Assert.assertNotNull(response.getId());
    }

}
