package com.yueyedexue.gulimall.product.vo.webvo;

import com.yueyedexue.gulimall.product.vo.Attr;
import lombok.Data;
import lombok.ToString;

import java.util.List;



@Data
@ToString
public class SpuItemAttrGroupVo {

    private String groupName;

    private List<Attr> attrs;

}
