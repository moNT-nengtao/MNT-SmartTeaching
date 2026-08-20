package com.smartteaching.common.vo;

import lombok.Data;
import java.util.List;

@Data
public class OrgTreeVO {

    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String type; // college / major / class
    private Integer studentCount;
    private Integer gradeYear;

    // 仅班级节点使用：班级下学生简表
    private List<OrgTreeVO.StudentItem> students;

    private List<OrgTreeVO> children;

    /**
     * 班级内学生子项
     */
    @Data
    public static class StudentItem {
        private Long id;
        private String realName;
        private String username;
    }
}
