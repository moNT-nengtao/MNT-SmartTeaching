package com.smartteaching.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrgDTO {

    public interface Group {
        interface Add {}
        interface Update {}
    }

    @NotNull(groups = OrgDTO.Group.Update.class, message = "id不能为空")
    private Long id;

    @NotBlank(groups = {OrgDTO.Group.Add.class, OrgDTO.Group.Update.class}, message = "节点类型不能为空")
    private String type;

    private Long parentId;

    @NotBlank(groups = {OrgDTO.Group.Add.class, OrgDTO.Group.Update.class}, message = "组织名称不能为空")
    private String name;

    private String code;

    private Integer gradeYear;

    private Integer sort;
}