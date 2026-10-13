package com.smartteaching.common.vo.org;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;


/**
 * @ClassName OrgCollegeListVO
 * @Description 学院返回对象，下拉选择 + 学院管理列表页面共用
 * @Author MNT
 * @Date 2026/8/16 09:08
 **/
@Data
public class OrgCollegeListVO {
    private Long id;

    /**
     * 学院名称
     */
    private String name;

    /**
     * 学院编码
     */
    private String code;

    /**
     * 父级id
     */
    private Long parentId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0禁用 1正常
     */
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
