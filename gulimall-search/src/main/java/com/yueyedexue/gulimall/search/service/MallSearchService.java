package com.yueyedexue.gulimall.search.service;

import com.yueyedexue.gulimall.search.vo.SearchParam;
import com.yueyedexue.gulimall.search.vo.SearchResult;

public interface MallSearchService {

    /**
     *
     * @param param 请求的所有检索条件
     * @return 检索结果以及分页数据
     */
    SearchResult search(SearchParam param);

}
