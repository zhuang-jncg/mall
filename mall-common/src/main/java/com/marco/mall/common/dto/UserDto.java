package com.marco.mall.common.dto;

import lombok.*;

import java.util.List;

/**
 * @auther macrozheng
 * @description 权限框架中使用的用户信息封装类
 * @date 2024/1/30
 * @github https://github.com/macrozheng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Builder
public class UserDto {
    private Long id;
    private String username;
    private String clientId;
    private List<String> permissionList;
}
