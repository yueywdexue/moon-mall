package com.yueyedexue.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.yueyedexue.common.to.es.SkuEsModel;
import com.yueyedexue.gulimall.search.config.GulimallElasticSearchConfiguration;
import com.yueyedexue.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/8 10:06
 **/

@Slf4j
@Service("productSaveServiceImpl")
public class ProductSaveServiceImpl implements ProductSaveService {

    @Resource
    RestHighLevelClient client;

    @Override
    public boolean productStatusUp(List<SkuEsModel> esModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel esModel : esModels) {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.index("product");
            indexRequest.id(esModel.getSkuId().toString());
            indexRequest.source(JSON.toJSONString(esModel), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = client.bulk(bulkRequest, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
        boolean b = bulk.hasFailures();
        List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
        if (b) {
            log.error("商品上架出现错误: {}", collect);
        }
        return b;
    }
}
