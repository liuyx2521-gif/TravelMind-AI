package com.travelmind.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("user")
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private String username;
    private String email;
    private String avatar;
    private String password;
}
