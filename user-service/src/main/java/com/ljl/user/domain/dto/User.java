package com.ljl.user.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    // Id
    private Long id;
    // 用户名
    private String userName;
    // 密码
    private String password;
    // 姓名
    private String name;
    // 年龄
    private Integer age;
    // 性别，1男性，2女性
    private Integer sex;
    // 出生日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date birthday;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date created;
    // 更新时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date updated;
    // 备注
    private String note;
}
