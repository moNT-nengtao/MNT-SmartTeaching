package com.smartteaching.common.dto.user;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @ClassName UserExcelDTO
 * @Description Excel导入用户实体，映射导入模板字段并记录行号用于错误定位
 * @Author MNT
 * @Date 2026/8/15 10:23
 **/
@Data
public class UserExcelDTO {

    @ExcelProperty("账号")
    private String username;

    @ExcelProperty("姓名")
    private String realName;

    @ExcelProperty("密码")
    private String password;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("角色")
    private String role;

    @ExcelProperty("手机号")
    private String phone;

    @ExcelProperty("邮箱")
    private String email;

    @ExcelProperty("学院")
    private String collegeName;

    @ExcelProperty("专业")
    private String majorName;

    @ExcelProperty("班级")
    private String className;

    // 记录Excel行号
    @ExcelIgnore  // 不映射Excel列
    private Integer rowNum;
}