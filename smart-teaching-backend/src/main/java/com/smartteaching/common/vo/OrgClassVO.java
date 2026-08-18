package com.smartteaching.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 班级VO
 */
@Data
@Schema(description = "班级返回对象")
public class OrgClassVO {

    @Schema(description = "班级id")
    private Long id;

    @Schema(description = "所属专业id")
    private Long majorId;

    @Schema(description = "班级名称")
    private String name;

    @Schema(description = "班级编码")
    private String code;

    @Schema(description = "父级id")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;

    @Schema(description = "年级")
    private Integer gradeYear;
}
