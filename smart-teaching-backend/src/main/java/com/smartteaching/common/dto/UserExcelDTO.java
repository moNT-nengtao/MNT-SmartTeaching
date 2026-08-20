package com.smartteaching.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel 导入用户实体
 */
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

    //不传name，业务处理太麻烦了,Excel转好了再导进来
    @ExcelProperty("学院ID")
    private Long collegeId;

    @ExcelProperty("专业ID")
    private Long majorId;

    @ExcelProperty("班级ID")
    private Long classId;
}