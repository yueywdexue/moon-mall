package com.yueyedexue.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.yueyedexue.gulimall.search.config.GulimallElasticSearchConfiguration;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Resource
    RestHighLevelClient client;

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

    @Data
    static
    class User{
        private String name;
        private Integer age;
        private String gender;
    }
    @Data
    @ToString
    static
    class Account{
        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }
    @Test
    public void indexData() throws IOException {
        IndexRequest request = new IndexRequest("posts").id("1");
        User user = new User();
        user.setName("zhangsan");
        user.setAge(18);
        user.setGender("???");
        String jsonString = JSON.toJSONString(user);
        request.source(jsonString, XContentType.JSON);
        IndexResponse indexResponse = client.index(request, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
        System.out.println(indexResponse.toString());
    }

    @Test
    public void searchDate() throws IOException {
        // 1. ????????????search??????
        SearchRequest searchRequest = new SearchRequest("bank");

        // 2. ??????????????????
        // 2.1 ??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("address","mill"));

        // 2.2 ??????????????????
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age");
        AvgAggregationBuilder balanceAgg = AggregationBuilders.avg("balanceAgg").field("balance");
        searchSourceBuilder.aggregation(ageAgg);
        searchSourceBuilder.aggregation(balanceAgg);

        // 3. ??????search??????
        // 3.1 ??????????????????????????????????????????
        searchRequest.source(searchSourceBuilder);
        System.out.println("????????????: " + searchSourceBuilder);
        // 3.2 ????????????search
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfiguration.COMMON_OPTIONS);

        // 4. ??????????????????
        // 4.1 ??????????????????
        SearchHits hits = searchResponse.getHits();
        SearchHit[] hits1 = hits.getHits();
        for (SearchHit documentFields : hits1) {
            String sourceAsString = documentFields.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }
        // 4.2 ??????????????????
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAggTerms = aggregations.get("ageAgg");
        List<? extends Terms.Bucket> buckets = ageAggTerms.getBuckets();
        Avg balanceAgg1 = aggregations.get("balanceAgg");
        for (Terms.Bucket bucket : buckets) {
            System.out.println(bucket.getKeyAsString());
        }
        System.out.println("????????????: " + balanceAgg1.getValue());
    }

}
