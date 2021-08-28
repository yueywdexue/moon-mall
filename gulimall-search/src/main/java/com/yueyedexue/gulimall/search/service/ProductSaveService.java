package com.yueyedexue.gulimall.search.service;

import com.yueyedexue.common.to.es.SkuEsModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> esModels) throws IOException;
}
