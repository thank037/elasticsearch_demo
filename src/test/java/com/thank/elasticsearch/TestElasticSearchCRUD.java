package com.thank.elasticsearch;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * description: 测试Elastic Search 增删改查操作
 *
 * @author thank
 * 2018/1/10 14:55
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class TestElasticSearchCRUD {

    @Autowired
    private TransportClient client;

    private static final String INDEX = "people";
    private static final String TYPE = "man";

    /**
     * 根据ID查询文档
     */
    @Test
    public void get() {

        String id = "2";
        GetResponse response = this.client.prepareGet(INDEX, TYPE, id).get();
        Map<String, Object> source = response.getSource();
        System.out.println("-----" + source);
        Assert.assertNotNull(source);
    }


    /**
     * 保存文档
     */
    @Test
    public void save() {

        String name = "诺瓦尔";
        String country = "巴西";
        int age = 33;

        String birthday = "1981-03-22 11:11:11";

        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                    .field("name", name)
                    .field("country", country)
                    .field("age", age)
                    .field("birthday", birthday)
                    .endObject();

            IndexResponse response = this.client.prepareIndex(INDEX, TYPE).setSource(builder).get();
            Assert.assertNotNull(response.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 根据ID删除文档
     */
    @Test
    public void delete() {

        String id = "AWDe1VTFHmavHIW_8lAI";
        DeleteResponse response = this.client.prepareDelete(INDEX, TYPE, id).get();
        Assert.assertEquals("DELETED", response.getResult().toString());
    }


    /**
     * 更新文档
     */
    @Test
    public void update() {

        String id = "AWDe1e8yHmavHIW_8lAJ";
        String name = "C罗222";
        int age = 12;

        UpdateRequest request = new UpdateRequest(INDEX, TYPE, id);

        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                    .field("name", name)
                    .field("age", age)
                    .endObject();

            request.doc(builder);
            UpdateResponse response = this.client.update(request).get();
            Assert.assertEquals("UPDATED", response.getResult().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 复合查询: 根据"日期范围+国家名称"来查询文档
     */
    @Test
    public void query() {

        // 查询条件
        String country = "阿根廷";
        String beginDate = "1970-12-12";
        String endDate = "1999-12-12";

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.matchQuery("country", country));

        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("birthday");
        rangeQueryBuilder.from(beginDate).to(endDate);

        boolQueryBuilder.filter(rangeQueryBuilder);

        SearchRequestBuilder searchRequestBuilder = this.client.prepareSearch(INDEX).setTypes(TYPE)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolQueryBuilder)
                .setFrom(0).setSize(10);

        // JSON格式的请求体
        System.out.println("请求体: " + searchRequestBuilder);
        SearchResponse response = searchRequestBuilder.get();

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (SearchHit hit: response.getHits()) {
            resultList.add(hit.getSourceAsMap());
        }

        System.out.println("查询结果: " + resultList);
        Assert.assertNotEquals(0, resultList.size());
    }
}
