package com.yueyedexue.common.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 会员
 * 
 * @author yueyedexue
 * @email 3295088274@qq.com
 * @date 2021-07-22 19:51:52
 */
@Data
public class MemberRespVo implements Serializable {

	private Long id;
	private Long levelId;
	private String username;
	private String password;
	private String nickname;
	private String mobile;
	private String email;
	private String header;
	private Integer gender;
	private Date birth;
	private String city;
	private String job;
	private String sign;
	private Integer sourceType;
	private Integer integration;
	private Integer growth;
	private Integer status;
	private Date createTime;
	private String socialUid;
	private String accessToken;
	private String expiresIn;

}
