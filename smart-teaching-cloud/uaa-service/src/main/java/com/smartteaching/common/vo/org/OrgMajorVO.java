package com.smartteaching.common.vo.org;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @ClassName OrgMajorVO
 * @Description 专业返回对象
 * @Author MNT
 * @Date 2026/8/16 13:44
 **/
@Data
@Schema(description = "专业返回对象")
public class OrgMajorVO {

    @Schema(description = "专业id")
    private Long id;

    @Schema(description = "所属学院id")
    private Long collegeId;

    @Schema(description = "专业名称")
    private String name;

    @Schema(description = "专业编码")
    private String code;

    @Schema(description = "父级id")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态 0禁用 1启用")
    private Integer status;
}
