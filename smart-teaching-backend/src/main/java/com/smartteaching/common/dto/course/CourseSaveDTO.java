package com.smartteaching.common.dto.course;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseSaveDTO {

    @NotNull(message = "课程ID不能为空", groups = {EditGroup.class})
    @Null(message = "新增时不能指定ID", groups = {AddGroup.class})
    private Long id;

    @NotBlank(message = "课程编号不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 64, message = "课程编号长度不能超过64位", groups = {AddGroup.class, EditGroup.class})
    private String code;

    @NotBlank(message = "课程名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 128, message = "课程名称长度不能超过128位", groups = {AddGroup.class, EditGroup.class})
    private String name;

    @NotNull(message = "授课教师ID不能为空", groups = {AddGroup.class, EditGroup.class})
    private Long teacherId;

    @NotNull(message = "学分不能为空", groups = {AddGroup.class, EditGroup.class})
    @Min(value = 0, message = "学分不能为负数", groups = {AddGroup.class, EditGroup.class})
    @Max(value = 20, message = "学分不能超过20", groups = {AddGroup.class, EditGroup.class})
    private BigDecimal credit;

    @Size(max = 32, message = "学期长度不能超过32位", groups = {AddGroup.class, EditGroup.class})
    private String semester;

    @NotNull(message = "选课容量不能为空", groups = {AddGroup.class, EditGroup.class})
    @Min(value = 0, message = "选课容量不能为负数", groups = {AddGroup.class, EditGroup.class})
    private Integer capacity;

    @Size(max = 150, message = "课程描述不能超过150字")
    private String description;

    public interface AddGroup {}

    public interface EditGroup {}
}
