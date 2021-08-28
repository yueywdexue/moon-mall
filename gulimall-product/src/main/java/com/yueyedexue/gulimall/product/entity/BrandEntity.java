package com.yueyedexue.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;

import com.yueyedexue.common.valid.AddGroup;
import com.yueyedexue.common.valid.ListValue;
import com.yueyedexue.common.valid.UpdateGroup;
import com.yueyedexue.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 17:17:11
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(groups = {UpdateGroup.class})
    @Null(groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名不能为空", groups = {AddGroup.class, UpdateGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotEmpty(message = "logo不能为空", groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的URL", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     * 介绍
     */
    @NotBlank(message = "介绍不能为空", groups = {AddGroup.class})
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
//	@Pattern(regexp = "/^[01]$/", message = "显示状态必须是0,1")
    @NotNull(message = "显示状态不能为空", groups = {AddGroup.class, UpdateStatusGroup.class})
    @ListValue(value = {0, 1}, groups = {UpdateGroup.class, AddGroup.class, UpdateStatusGroup.class}, message = "showStatus只能是指定的值")
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(message = "检索首字母不能为空", groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;

    /**
     * 排序
     */
    @NotNull(message = "排序字段不能为空", groups = {AddGroup.class})
    @Min(value = 0, message = "排序字段最小为0", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
