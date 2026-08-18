package com.smartteaching.common.dto;

import lombok.Data;

/**
 * 用户列表查询条件DTO
 */
@Data
public class UserQueryDTO {
    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 角色
     */
    private String role;

    /**
     * 用户状态
     */
    private Integer status;
}
