package com.smartteaching.common.utils;

import com.smartteaching.common.dto.OrgExcelDTO;
import com.smartteaching.entity.org.College;
import com.smartteaching.entity.org.Major;
import com.smartteaching.entity.org.ClassInfo;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 组织架构 Excel 导入校验工具类
 */
public class OrgExcelUtil {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]*$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-\\s]+$");

    /**
     * 校验 Excel 导入的组织数据
     */
    public static List<String> validateOrg(String type, String name, String code,
                                           Long parentId, Integer gradeYear) {
        List<String> errors = new ArrayList<>();

        // 1. 类型校验
        if (!"college".equals(type) && !"major".equals(type) && !"class".equals(type)) {
            errors.add("类型必须为 college/major/class");
            return errors;
        }

        // 2. 名称校验
        if (!StringUtils.hasText(name)) {
            errors.add("名称不能为空");
        } else {
            if (name.length() < 1 || name.length() > 50) {
                errors.add("名称长度1-50字符");
            }
            if (!NAME_PATTERN.matcher(name).matches()) {
                errors.add("名称包含非法字符");
            }
        }

        // 3. 编码校验（选填）
        if (StringUtils.hasText(code)) {
            if (code.length() > 30) {
                errors.add("编码长度不能超过30字符");
            }
            if (!CODE_PATTERN.matcher(code).matches()) {
                errors.add("编码仅字母数字下划线中划线");
            }
        }

        // 4. 父级ID校验
        if ("college".equals(type)) {
            if (parentId == null || parentId != 0) {
                errors.add("学院的父级ID必须为0");
            }
        } else if ("major".equals(type)) {
            if (parentId == null || parentId <= 0) {
                errors.add("专业必须关联学院，父级ID（学院ID）必须大于0");
            }
        } else if ("class".equals(type)) {
            if (parentId == null || parentId <= 0) {
                errors.add("班级必须关联专业，父级ID（专业ID）必须大于0");
            }
            if (gradeYear == null) {
                errors.add("年级不能为空");
            } else if (gradeYear < 2000 || gradeYear > 2099) {
                errors.add("年级格式错误，请输入4位年份（如2026）");
            }
        }

        return errors;
    }

    /**
     * 格式化错误信息
     */
    public static String formatError(int rowNum, String name, List<String> errors) {
        String displayName = StringUtils.hasText(name) ? name : "空名称";
        return String.format("第 %d 行 (%s) 错误: %s", rowNum, displayName, String.join("；", errors));
    }

    /**
     * 构建 College 对象
     */
    public static College buildCollege(OrgExcelDTO data) {
        College college = new College();
        college.setName(data.getName());
        college.setCode(data.getCode());
        college.setParentId(0L);
        college.setSort(0);
        college.setStatus(1);
        return college;
    }

    /**
     * 构建 Major 对象
     */
    public static Major buildMajor(OrgExcelDTO data) {
        Major major = new Major();
        major.setName(data.getName());
        major.setCode(data.getCode());
        major.setCollegeId(data.getParentId());
        major.setSort(0);
        major.setStatus(1);
        return major;
    }

    /**
     * 构建 ClassInfo 对象
     */
    public static ClassInfo buildClassInfo(OrgExcelDTO data) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName(data.getName());
        classInfo.setCode(data.getCode());
        classInfo.setMajorId(data.getParentId());
        classInfo.setGradeYear(data.getGradeYear());
        classInfo.setSort(0);
        classInfo.setStatus(1);
        return classInfo;
    }

    /**
     * 根据类型构建对应的实体对象
     */
    public static Object buildOrgEntity(OrgExcelDTO data) {
        String type = data.getType();
        if ("college".equals(type)) {
            return buildCollege(data);
        } else if ("major".equals(type)) {
            return buildMajor(data);
        } else if ("class".equals(type)) {
            return buildClassInfo(data);
        }
        return null;
    }
}