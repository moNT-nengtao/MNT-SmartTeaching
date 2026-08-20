package com.smartteaching.common.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.smartteaching.common.dto.OrgExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * 组织 Excel 导入工具类
 *
 * 功能：
 * 1. readExcelData        - 读取Excel文件
 * 2. validateOrgData      - 校验单行数据(name、code、affiliationName、gradeYear)
 * 3. classifyByType       - 按类型分类+文件内重复检查
 */
@Slf4j
public class OrgExcelUtil {

    private static final String TYPE_COLLEGE = "college";
    private static final String TYPE_MAJOR = "major";
    private static final String TYPE_CLASS = "class";

    /**
     * 读取 Excel 文件
     */
    public static List<OrgExcelDTO> readExcelData(MultipartFile file) {
        try {
            List<OrgExcelDTO> dataList = new ArrayList<>();
            EasyExcel.read(file.getInputStream(), OrgExcelDTO.class, new ReadListener<OrgExcelDTO>() {
                @Override
                public void invoke(OrgExcelDTO data, AnalysisContext context) {
                    dataList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    log.info("Excel 读取完成，共 {} 条数据", dataList.size());
                }
            }).sheet().doRead();
            return dataList;
        } catch (IOException e) {
            log.error("读取 Excel 文件失败", e);
            throw new RuntimeException("读取 Excel 文件失败：" + e.getMessage());
        }
    }

    /**
     * 校验单行数据
     */
    public static List<String> validateOrgData(OrgExcelDTO data, int rowNum) {
        List<String> errors = new ArrayList<>();

        String type = data.getType();
        if (type == null || type.trim().isEmpty()) {
            errors.add("第 " + rowNum + " 行：类型不能为空");
        } else {
            String lowerType = type.trim().toLowerCase();
            if (!TYPE_COLLEGE.equals(lowerType) && !TYPE_MAJOR.equals(lowerType) && !TYPE_CLASS.equals(lowerType)) {
                errors.add("第 " + rowNum + " 行：类型错误，只能为 college/major/class");
            }
        }

        String name = data.getName();
        if (name == null || name.trim().isEmpty()) {
            errors.add("第 " + rowNum + " 行：名称不能为空");
        }

        String code = data.getCode();
        if (code != null && !code.trim().isEmpty() && code.length() > 64) {
            errors.add("第 " + rowNum + " 行：编码长度不能超过64位");
        }

        String typeLower = type != null ? type.trim().toLowerCase() : "";
        String affiliation = data.getAffiliationName();
        if (TYPE_MAJOR.equals(typeLower) || TYPE_CLASS.equals(typeLower)) {
            if (affiliation == null || affiliation.trim().isEmpty()) {
                String typeName = TYPE_MAJOR.equals(typeLower) ? "专业" : "班级";
                errors.add("第 " + rowNum + " 行：" + typeName + " 的所属不能为空");
            }
        }

        if (TYPE_CLASS.equals(typeLower) && data.getGradeYear() == null) {
            errors.add("第 " + rowNum + " 行：班级的年级不能为空");
        }

        return errors;
    }

    /**
     * 按类型分类，并检查文件内重复
     */
    public static ClassifyResult classifyByType(List<OrgExcelDTO> dataList) {
        ClassifyResult result = new ClassifyResult();

        Map<String, List<Integer>> collegeRowMap = new HashMap<>();
        Map<String, List<Integer>> majorRowMap = new HashMap<>();
        Map<String, List<Integer>> classRowMap = new HashMap<>();

        for (int i = 0; i < dataList.size(); i++) {
            OrgExcelDTO data = dataList.get(i);
            int rowNum = i + 2;
            String type = data.getType() != null ? data.getType().trim().toLowerCase() : "";
            String name = data.getName() != null ? data.getName().trim() : "";

            if (name.isEmpty()) continue;

            switch (type) {
                case TYPE_COLLEGE:
                    result.colleges.add(data);
                    collegeRowMap.computeIfAbsent(name, k -> new ArrayList<>()).add(rowNum);
                    break;
                case TYPE_MAJOR:
                    result.majors.add(data);
                    majorRowMap.computeIfAbsent(name, k -> new ArrayList<>()).add(rowNum);
                    break;
                case TYPE_CLASS:
                    result.classes.add(data);
                    classRowMap.computeIfAbsent(name, k -> new ArrayList<>()).add(rowNum);
                    break;
                default:
                    break;
            }
        }

        // 检查重复
        for (Map.Entry<String, List<Integer>> entry : collegeRowMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.duplicateErrors.add("学院 \"" + entry.getKey() + "\" 重复，行号：" + entry.getValue());
            }
        }
        for (Map.Entry<String, List<Integer>> entry : majorRowMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.duplicateErrors.add("专业 \"" + entry.getKey() + "\" 重复，行号：" + entry.getValue());
            }
        }
        for (Map.Entry<String, List<Integer>> entry : classRowMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.duplicateErrors.add("班级 \"" + entry.getKey() + "\" 重复，行号：" + entry.getValue());
            }
        }

        log.info("分类完成：学院 {} 条，专业 {} 条，班级 {} 条，重复错误 {} 条",
                result.colleges.size(), result.majors.size(), result.classes.size(), result.duplicateErrors.size());

        return result;
    }

    // ==================== 内部类 ====================

    public static class ClassifyResult {
        public List<OrgExcelDTO> colleges = new ArrayList<>();
        public List<OrgExcelDTO> majors = new ArrayList<>();
        public List<OrgExcelDTO> classes = new ArrayList<>();
        public List<String> duplicateErrors = new ArrayList<>();

        public boolean isEmpty() {
            return colleges.isEmpty() && majors.isEmpty() && classes.isEmpty();
        }

        public boolean hasDuplicateErrors() {
            return !duplicateErrors.isEmpty();
        }
    }
}