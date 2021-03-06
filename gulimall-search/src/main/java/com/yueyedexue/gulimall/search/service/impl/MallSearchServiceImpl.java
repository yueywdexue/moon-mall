package com.yueyedexue.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.yueyedexue.common.to.es.SkuEsModel;
import com.yueyedexue.gulimall.search.config.GulimallElasticSearchConfiguration;
import com.yueyedexue.gulimall.search.constant.EsConstant;
import com.yueyedexue.gulimall.search.service.MallSearchService;
import com.yueyedexue.gulimall.search.vo.SearchParam;
import com.yueyedexue.gulimall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: MoonNightSnow
 * @createTime: 2021/8/11 19:38
 **/
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Resource
    RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam param) {
        SearchRequest searchRequest = null;
        // 1. ??????????????????
        searchRequest = this.getSearchRequest(param);
        SearchResponse searchResponse = null;
        try {
            // 2. ????????????
            searchResponse = client.search(searchRequest, GulimallElasticSearchConfiguration.COMMON_OPTIONS);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 3. ??????????????????, ?????????SearchResult
        SearchResult searchResult = null;
        assert searchResponse != null;
        searchResult = this.getResponseResult(searchResponse, param);

        return searchResult;
    }

    /**
     * ??????es????????????????????????????????????
     *
     * @param searchResponse es??????????????????
     * @return ?????????????????????????????????
     */
    private SearchResult getResponseResult(SearchResponse searchResponse, SearchParam param) {
        SearchResult result = new SearchResult();

        SearchHits hits = searchResponse.getHits();
        // ????????????????????????
        List<SkuEsModel> skuEsModels = new ArrayList<>();
        for (SearchHit hit : hits.getHits()) {
            String source = hit.getSourceAsString();
            SkuEsModel skuEsModel = JSON.parseObject(source, SkuEsModel.class);
            if (!StringUtils.isEmpty(param.getKeyword())) {
                String skuTitle = hit.getHighlightFields().get("skuTitle").getFragments()[0].string();
                skuEsModel.setSkuTitle(skuTitle);
            }
            skuEsModels.add(skuEsModel);
        }
        result.setProduct(skuEsModels);

        // ??????3????????? ??????????????????????????????
        // 1 ????????????????????????
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested attr_agg = searchResponse.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        for (Terms.Bucket bucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 1 ????????????id
            long attrId = bucket.getKeyAsNumber().longValue();
            // 2 ???????????????
            String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            // 3 ???????????????
            List<String> attrValues = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item -> ((Terms.Bucket) item).getKeyAsString()).collect(Collectors.toList());
            // ??????AttrVo??????
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValues);

            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);

        // 2 ????????????????????????
        ParsedLongTerms brand_agg = searchResponse.getAggregations().get("brand_agg");
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        List<? extends Terms.Bucket> brandBuckets = brand_agg.getBuckets();
        for (Terms.Bucket bucket : brandBuckets) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // ????????????id
            long brandId = bucket.getKeyAsNumber().longValue();
            // ????????????????????????????????????
            String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            // ????????????????????????????????????
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            // ??????brandVo??????
            brandVo.setBrandId(brandId);
            brandVo.setBrandName(brandName);
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        // 3 ??????????????????????????????
        ParsedLongTerms catalog_agg = searchResponse.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalog_agg.getBuckets();
        for (Terms.Bucket bucket : buckets) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // ????????????id
            String catalogId = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(catalogId));
            // ????????????????????????????????????
            ParsedStringTerms catalog_name_agg = bucket.getAggregations().get("catalog_name_agg");
            String catalogName = catalog_name_agg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        // ????????????
        int total = (int) hits.getTotalHits().value;
        int totalPage = total % EsConstant.PRODUCT_PAGE_SIZE == 0 ? (total / EsConstant.PRODUCT_PAGE_SIZE) : (total / EsConstant.PRODUCT_PAGE_SIZE + 1);
        result.setTotalPages(totalPage);
        result.setPageNum(param.getPageNum());
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);
        return result;
    }

    /**
     * ??????????????????
     * ?????????????????? ????????????(??????id  hasStock skuPrice brandId attrs) ???????????? ?????? ????????????
     *
     * @param param ???????????????????????????
     * @return ???????????????????????? SearchRequest
     */
    private SearchRequest getSearchRequest(SearchParam param) {

        // ??????????????????
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // ????????????
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        // ??????id ??????????????????????????????
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        // brandId ????????????????????????
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        // hasStock ????????????????????????
        if (param.getHasStock() != null) {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }
        // skuPrice ???????????? skuPrice=0_5000/_5000/0_
        String skuPrice = param.getSkuPrice();
        if (!StringUtils.isEmpty(skuPrice)) {
            String[] price = skuPrice.split("_");
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            if (price.length == 2) {
                rangeQuery.gte(price[0]).lte(price[1]);
            } else {
                if (skuPrice.startsWith("_")) {
                    rangeQuery.lte(price[0]);
                }
                if (skuPrice.endsWith("_")) {
                    rangeQuery.gte(price[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }
        // attrs ???????????? attrs=1_5???:6???
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attrStr : param.getAttrs()) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] attr = attrStr.split("_");
                String attrId = attr[0]; // ??????id
                String[] attrValue = attr[1].split(":"); // ?????????
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        sourceBuilder.query(boolQuery);
        /**
         *
         * ??????, ??????, ??????
         */

        // ??????
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        // ??????
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        // ????????????
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }
        /**
         * ????????????
         */

        // 1. ????????????
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId").size(50);
        // 1.1 ???????????????
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        // TODO 1 ???????????? brand_agg
        sourceBuilder.aggregation(brand_agg);
        // 2. ????????????
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(30);
        // 2.1 ???????????????
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        // TODO 2 ???????????? catalog_agg
        sourceBuilder.aggregation(catalog_agg);
        // 3. ????????????
        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        // 3.1 ????????????????????????id
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId").size(50);
        // ????????????id??????????????????
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        // ????????????id??????????????????
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        // ????????????????????? attr_id_agg
        attr_agg.subAggregation(attr_id_agg);
        // TODO ???????????? attr_agg
        sourceBuilder.aggregation(attr_agg);

//        String string = sourceBuilder.toString();
//        System.out.println("DSL: " + string);


        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
    }
}
