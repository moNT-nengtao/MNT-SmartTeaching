package com.smartteaching.service.org;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartteaching.common.constant.MessageConstant;
import com.smartteaching.common.dto.org.OrgDTO;
import com.smartteaching.common.dto.org.OrgExcelDTO;
import com.smartteaching.common.vo.org.OrgExportVO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.utils.OrgExcelUtil;
import com.smartteaching.common.vo.org.OrgClassVO;
import com.smartteaching.common.vo.org.OrgCollegeListVO;
import com.smartteaching.common.vo.org.OrgMajorVO;
import com.smartteaching.common.vo.org.OrgTreeVO;
import com.smartteaching.entity.org.ClassInfo;
import com.smartteaching.entity.org.College;
import com.smartteaching.entity.org.Major;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.OrgMapper;
import com.smartteaching.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName OrgServiceImpl
 * @Description 组织服务实现类
 * @Author MNT
 * @Date 2026/8/16 10:18
 **/
@Service
@Slf4j
public class OrgServiceImpl implements OrgService {

    @Autowired
    private OrgMapper orgMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取学院列表
     * @param isSelect 是否为下拉选择场景 true:下拉,false:页面列表
     * @return 学院VO集合
     */
    @Override
    public List<OrgCollegeListVO> getOrgCollegeList(boolean isSelect) {
        Integer status = isSelect ? 1 : null;
        List<College> entityList = orgMapper.selectCollegeList(status);

        return entityList.stream()
                .map(entity -> {
                    OrgCollegeListVO vo = new OrgCollegeListVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .toList();
    }

    /**
     * 获取专业列表
     *
     * @param collegeId 学院id，可为null
     * @return 专业VO集合
     */
    @Override
    public List<OrgMajorVO> getOrgMajorList(Long collegeId) {
        List<Major> entityList = orgMapper.selectMajorList(collegeId,1);

        return entityList.stream()
                .map(entity -> {
                    OrgMajorVO vo = new OrgMajorVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .toList();
    }

    /**
     * 获取班级列表
     * @param majorId 专业id，可为null
     * @param isSelect
     * @return 班级VO集合
     */
    @Override
    public List<OrgClassVO> getOrgClassList(Long majorId, boolean isSelect) {
        Integer status = isSelect ? 1 : null;
        List<ClassInfo> entityList = orgMapper.selectClassList(majorId, status);

        return entityList.stream()
                .map(entity -> {
                    OrgClassVO vo = new OrgClassVO();
                    BeanUtils.copyProperties(entity, vo);
                    return vo;
                })
                .toList();
    }

    /**
     * 组织树
     * @return
     */
    @Override
    public List<OrgTreeVO> bulidOrgTree() {
        //学院
        List<College> collegeList = orgMapper.selectCollegeList(null);
        if(CollectionUtils.isEmpty(collegeList)){
            return new ArrayList<>();
        }

        //专业
        List<Major> majorList =orgMapper.selectMajorList(null, 1);
        Map<Long,List<Major>> majorByCollegeIdMap = majorList.stream().collect(Collectors.groupingBy(Major::getCollegeId));

        //班级
        List<ClassInfo> classInfoList = orgMapper.selectClassList(null, 1);
        Map<Long,List<ClassInfo>> classByMajorIdMap = classInfoList.stream().collect(Collectors.groupingBy(ClassInfo::getMajorId));

        //学生
        LambdaQueryWrapper<User> queryStudentWrapper = Wrappers.lambdaQuery();
        queryStudentWrapper.eq(User::getRole,"student")
                .eq(User::getStatus,1)
                .isNotNull(User::getClassId);
        List<User> studentList = userMapper.selectList(queryStudentWrapper);
        Map<Long, List<OrgTreeVO.StudentItem>> classStudentMap = studentList.stream()
                .collect(Collectors.groupingBy(
                        User::getClassId,
                        Collectors.mapping(user -> {
                            OrgTreeVO.StudentItem item = new OrgTreeVO.StudentItem();
                            item.setId(user.getId());
                            item.setRealName(user.getRealName());
                            item.setUsername(user.getUsername());
                            return item;
                        }, Collectors.toList())
                ));


        //组装树
        List<OrgTreeVO> orgTreeResult = new ArrayList<>();
        for (College college : collegeList) {
            OrgTreeVO collegeVo = new OrgTreeVO();
            BeanUtils.copyProperties(college, collegeVo);
            collegeVo.setType("college");
            collegeVo.setStudentCount(0);
            collegeVo.setChildren(new ArrayList<>());

            List<Major> majorOfCollege = majorByCollegeIdMap.getOrDefault(college.getId(), new ArrayList<>());
            for (Major major : majorOfCollege) {
                OrgTreeVO majorVo = new OrgTreeVO();
                BeanUtils.copyProperties(major, majorVo);
                majorVo.setType("major");
                majorVo.setStudentCount(0);
                majorVo.setChildren(new ArrayList<>());

                List<ClassInfo> classInfos = classByMajorIdMap.getOrDefault(major.getId(), new ArrayList<>());
                for (ClassInfo classInfo : classInfos) {
                    OrgTreeVO classVo = new OrgTreeVO();
                    BeanUtils.copyProperties(classInfo, classVo);
                    classVo.setType("class");
                    classVo.setStudentCount(classInfo.getStudentCount() == null ? 0 : classInfo.getStudentCount());
                    classVo.setChildren(new ArrayList<>());

                    List<OrgTreeVO.StudentItem> studentItems = classStudentMap.getOrDefault(classInfo.getId(), new ArrayList<>());
                    classVo.setStudents(studentItems);

                    majorVo.getChildren().add(classVo);
                }
                collegeVo.getChildren().add(majorVo);
            }
            orgTreeResult.add(collegeVo);
        }
        return orgTreeResult;
    }

    /**
     * 新增组织节点
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OrgDTO dto) {
        String name = dto.getName().trim();
        String type = dto.getType();
        Integer sort = 0;
        //学院节点
        if ("college".equals(type)) {
            College existing = orgMapper.getCollegeByNameAllStatus(name);
            //检查有没有禁用的同名数据，启用然后覆盖了
            if (existing != null) {
                if (existing.getStatus() == 1) throw new BaseException(MessageConstant.ALREADY_EXISTS + "：" + name);
                existing.setStatus(1);
                existing.setCode(dto.getCode());
                existing.setParentId(dto.getParentId());
                existing.setSort(sort);
                existing.setUpdateTime(LocalDateTime.now());
                if (orgMapper.updateCollege(existing) <= 0) throw new BaseException(MessageConstant.REVERT_COLLEGE_FAIL);
                return;
            }
            College college = new College();
            college.setName(name);
            college.setCode(dto.getCode());
            college.setParentId(dto.getParentId());
            college.setSort(sort);
            college.setStatus(1);
            if (orgMapper.saveCollege(college) <= 0) throw new BaseException(MessageConstant.ADD_ORG_NODE_FAIL);
        //专业节点
        } else if ("major".equals(type)) {
            Long collegeId = dto.getParentId();
            College college = orgMapper.getCollegeById(collegeId);
            if (college == null || college.getStatus() != 1) throw new BaseException(MessageConstant.COLLEGE_NOT_EXIST_OR_DISABLED);

            Major existing = orgMapper.getMajorByNameAllStatus(name);
            if (existing != null) {
                if (existing.getStatus() == 1) throw new BaseException(MessageConstant.ALREADY_EXISTS + "：" + name);
                existing.setStatus(1);
                existing.setCollegeId(collegeId);
                existing.setCode(dto.getCode());
                existing.setSort(sort);
                existing.setUpdateTime(LocalDateTime.now());
                if (orgMapper.updateMajor(existing) <= 0) throw new BaseException(MessageConstant.REVERT_MAJOR_FAIL);
                return;
            }
            Major major = new Major();
            major.setName(name);
            major.setCode(dto.getCode());
            major.setCollegeId(collegeId);
            major.setSort(sort);
            major.setStatus(1);
            if (orgMapper.saveMajor(major) <= 0) throw new BaseException(MessageConstant.ADD_ORG_NODE_FAIL);
        //班级节点
        } else if ("class".equals(type)) {
            Long majorId = dto.getParentId();
            Integer gradeYear = dto.getGradeYear();
            if (gradeYear == null) throw new BaseException(MessageConstant.CLASS_GRADE_YEAR_NOT_NULL);

            Major major = orgMapper.getMajorById(majorId);
            if (major == null || major.getStatus() != 1) throw new BaseException(MessageConstant.MAJOR_NOT_EXIST_OR_DISABLED);

            ClassInfo existing = orgMapper.getClassByNameAllStatus(name);
            if (existing != null) {
                if (existing.getStatus() == 1) throw new BaseException(MessageConstant.ALREADY_EXISTS + "：" + name);
                existing.setStatus(1);
                existing.setMajorId(majorId);
                existing.setCode(dto.getCode());
                existing.setGradeYear(gradeYear);
                existing.setSort(sort);
                existing.setUpdateTime(LocalDateTime.now());
                if (orgMapper.updateClass(existing) <= 0) throw new BaseException(MessageConstant.REVERT_CLASS_FAIL);
                return;
            }
            ClassInfo classInfo = new ClassInfo();
            classInfo.setName(name);
            classInfo.setCode(dto.getCode());
            classInfo.setMajorId(majorId);
            classInfo.setGradeYear(gradeYear);
            classInfo.setSort(sort);
            classInfo.setStatus(1);
            if (orgMapper.saveClass(classInfo) <= 0) throw new BaseException(MessageConstant.ADD_ORG_NODE_FAIL);

        } else {
            throw new BaseException(MessageConstant.ORG_TYPE_ILLEGAL);
        }
    }

    /**
     * 编辑组织节点
     * @param dto
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(OrgDTO dto) {
        String type = dto.getType();
        String name = dto.getName().trim();
        Long id = dto.getId();
        Integer sort = dto.getSort() != null ? dto.getSort() : 0;

        //学院
        if ("college".equals(type)) {
            College old = orgMapper.getCollegeById(id);
            if (old == null) throw new BaseException(MessageConstant.COLLEGE_NOT_EXIST);
            //检查同名
            if (!old.getName().equals(name)) {
                College existing = orgMapper.getCollegeByNameAllStatus(name);
                if (existing != null) {
                    if (existing.getStatus() == 1) throw new BaseException(MessageConstant.ALREADY_EXISTS + "：" + name);
                    throw new BaseException(String.format(MessageConstant.DISABLED_COLLEGE_EXISTS, name));
                }
            }

            College college = new College();
            college.setId(id);
            college.setName(name);
            college.setCode(dto.getCode());
            college.setParentId(dto.getParentId());
            college.setSort(sort);
            college.setStatus(1);
            if (orgMapper.updateCollege(college) <= 0) throw new BaseException(MessageConstant.EDIT_ORG_NODE_FAIL);
            //专业
        } else if ("major".equals(type)) {
            Major old = orgMapper.getMajorById(id);
            if (old == null) throw new BaseException(MessageConstant.MAJOR_NOT_EXIST);

            Long collegeId = dto.getParentId();
            College college = orgMapper.getCollegeById(collegeId);
            if (college == null || college.getStatus() != 1) throw new BaseException(MessageConstant.COLLEGE_NOT_EXIST_OR_DISABLED);
            //检查同名
            if (!old.getName().equals(name)) {
                Major existing = orgMapper.getMajorByNameAllStatus(name);
                if (existing != null) {
                    if (existing.getStatus() == 1) throw new BaseException(MessageConstant.ALREADY_EXISTS + "：" + name);
                    throw new BaseException(String.format(MessageConstant.DISABLED_MAJOR_EXISTS, name));
                }
            }

            Major major = new Major();
            major.setId(id);
            major.setName(name);
            major.setCode(dto.getCode());
            major.setCollegeId(collegeId);
            major.setSort(sort);
            major.setStatus(1);
            if (orgMapper.updateMajor(major) <= 0) throw new BaseException(MessageConstant.EDIT_ORG_NODE_FAIL);
            //班级
        } else if ("class".equals(type)) {
            ClassInfo old = orgMapper.getClassById(id);
            if (old == null) throw new BaseException(MessageConstant.CLASS_NOT_EXIST);

            Integer gradeYear = dto.getGradeYear();
            if (gradeYear == null) throw new BaseException(MessageConstant.CLASS_GRADE_YEAR_NOT_NULL);

            Long majorId = dto.getParentId();
            Major major = orgMapper.getMajorById(majorId);
            if (major == null || major.getStatus() != 1) throw new BaseException(MessageConstant.MAJOR_NOT_EXIST_OR_DISABLED);
            //检查同名
            if (!old.getName().equals(name)) {
                ClassInfo existing = orgMapper.getClassByNameAllStatus(name);
                if (existing != null) {
                    if (existing.getStatus() == 1) throw new BaseException(MessageConstant.ALREADY_EXISTS + "：" + name);
                    throw new BaseException(String.format(MessageConstant.DISABLED_CLASS_EXISTS, name));
                }
            }

            ClassInfo classInfo = new ClassInfo();
            classInfo.setId(id);
            classInfo.setName(name);
            classInfo.setCode(dto.getCode());
            classInfo.setMajorId(majorId);
            classInfo.setGradeYear(gradeYear);
            classInfo.setSort(sort);
            classInfo.setStatus(1);
            if (orgMapper.updateClass(classInfo) <= 0) throw new BaseException(MessageConstant.EDIT_ORG_NODE_FAIL);

        } else {
            throw new BaseException(MessageConstant.ORG_TYPE_ILLEGAL);
        }
    }

    /**
     * 删除组织节点
     *
     * @param id
     * @param type
     */
    @Override
    public void delete(Long id, String type) {
        if (id == null) {
            throw new BaseException(MessageConstant.ORG_NODE_NOT_EXIST);
        }

        Long count;
        switch (type) {
            case "college":
                count = orgMapper.countMajorsByCollegeId(id);
                if (count != null && count > 0) {
                    throw new BaseException(MessageConstant.COLLEGE_HAS_MAJOR);
                }
                orgMapper.deleteCollegeById(id);
                break;
            case "major":
                count = orgMapper.countClassesByMajorId(id);
                if (count != null && count > 0) {
                    throw new BaseException(MessageConstant.MAJOR_HAS_CLASS);
                }
                orgMapper.deleteMajorById(id);
                break;
            case "class":
                count = orgMapper.countStudentsByClassId(id);
                if (count != null && count > 0) {
                    throw new BaseException(MessageConstant.CLASS_HAS_STUDENT);
                }
                orgMapper.deleteClassById(id);
                break;
            default:
                throw new BaseException(MessageConstant.ORG_TYPE_ILLEGAL + type);
        }
    }

    /**
     * 学生移出班级
     * @param ids
     */
    @Override
    public void studentsRemove(String ids) {
        if (ids == null) {
            throw new BaseException("未选择选项");
        }
        //转换格式
        List<String> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        if (idList.isEmpty()) {
            throw new RuntimeException("无有效id");
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(User::getId, idList)
                .set(User::getStatus, 0);
        userMapper.update(wrapper);
    }

    /**
     * 组织批量导入
     *
     * @param file
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Long> batchImportOrg(MultipartFile file) {
        List<OrgExcelDTO> dtoList = OrgExcelUtil.readExcelData(file);

        //校验
        if (dtoList == null || dtoList.isEmpty()) {
            throw new BaseException(MessageConstant.EXCEL_FILE_EMPTY);
        }
        List<String> allErrorList = new ArrayList<>();
        for (int i = 0; i < dtoList.size(); i++) {
            int rowNum = i + 2;
            List<String> errorList = OrgExcelUtil.validateOrgData(dtoList.get(i), rowNum);
            allErrorList.addAll(errorList);
        }
        if (!allErrorList.isEmpty()) {
            throw new BaseException("数据校验失败: " + String.join(",", allErrorList));
        }

        //分类
        OrgExcelUtil.ClassifyResult classifyResult = OrgExcelUtil.classifyByType(dtoList);
        if (classifyResult.hasDuplicateErrors()) {
            throw new BaseException("文件内数据重复：" + String.join("；", classifyResult.duplicateErrors));
        }
        if (classifyResult.isEmpty()) {
            throw new BaseException(MessageConstant.NO_VALID_DATA_IMPORT);
        }

        //学院 - 过滤掉已存在的
        Set<String> dbCollegeNames;
        if (!classifyResult.colleges.isEmpty()) {
            List<String> collegeNameList = classifyResult.colleges.stream()
                    .map(d -> d.getName().trim())
                    .collect(Collectors.toList());
            List<College> dbColleges = orgMapper.getCollegesByNames(collegeNameList);
            dbCollegeNames = dbColleges.stream()
                    .map(College::getName)
                    .collect(Collectors.toSet());
        } else {
            dbCollegeNames = new HashSet<>();
        }

        List<OrgExcelDTO> newCollegeList = classifyResult.colleges.stream()
                .filter(d -> !dbCollegeNames.contains(d.getName().trim()))
                .collect(Collectors.toList());

        List<College> newColleges = buildCollegeEntities(newCollegeList);
        Map<String, Long> collegeIdMap = new HashMap<>();
        if (!newColleges.isEmpty()) {
            orgMapper.batchInsertColleges(newColleges);
            log.info("新增学院 {} 条", newColleges.size());

            List<String> collegeNames = newColleges.stream()
                    .map(College::getName)
                    .collect(Collectors.toList());
            List<College> insertedColleges = orgMapper.getCollegesByNames(collegeNames);
            collegeIdMap = insertedColleges.stream()
                    .collect(Collectors.toMap(College::getName, College::getId, (a, b) -> a));
        }

        //allCollegeMap = 新增的 + 数据库已有的 + 本次Excel里有的
        Map<String, Long> allCollegeMap = new HashMap<>();
        allCollegeMap.putAll(collegeIdMap);
        Set<String> allCollegeNames = classifyResult.colleges.stream()
                .map(d -> d.getName().trim())
                .collect(Collectors.toSet());
        if (!allCollegeNames.isEmpty()) {
            List<College> existingColleges = orgMapper.getCollegesByNames(new ArrayList<>(allCollegeNames));
            Map<String, Long> existingMap = existingColleges.stream()
                    .collect(Collectors.toMap(College::getName, College::getId, (a, b) -> a));
            allCollegeMap.putAll(existingMap);
        }

        //专业 - 过滤掉已存在的
        Set<String> dbMajorNames;
        if (!classifyResult.majors.isEmpty()) {
            List<String> majorNameList = classifyResult.majors.stream()
                    .map(d -> d.getName().trim())
                    .collect(Collectors.toList());
            List<Major> dbMajors = orgMapper.getMajorsByNames(majorNameList);
            dbMajorNames = dbMajors.stream()
                    .map(Major::getName)
                    .collect(Collectors.toSet());
        } else {
            dbMajorNames = new HashSet<>();
        }

        List<OrgExcelDTO> newMajorList = classifyResult.majors.stream()
                .filter(d -> !dbMajorNames.contains(d.getName().trim()))
                .collect(Collectors.toList());

        //校验专业（只校验新增的）
        List<String> majorErrors = validateMajors(newMajorList, allCollegeMap);
        if (!majorErrors.isEmpty()) {
            throw new BaseException("专业数据校验失败：" + String.join("；", majorErrors));
        }

        List<Major> newMajors = buildMajorEntities(newMajorList, allCollegeMap);
        Map<String, Long> majorIdMap = new HashMap<>();
        if (!newMajors.isEmpty()) {
            orgMapper.batchInsertMajors(newMajors);
            log.info("新增专业 {} 条", newMajors.size());
            List<String> majorNames = newMajors.stream()
                    .map(Major::getName)
                    .collect(Collectors.toList());
            List<Major> insertedMajors = orgMapper.getMajorsByNames(majorNames);
            majorIdMap = insertedMajors.stream()
                    .collect(Collectors.toMap(Major::getName, Major::getId, (a, b) -> a));
        }

        //allMajorMap = 新增的 + 数据库已有的 + 本次Excel里有的
        Map<String, Long> allMajorMap = new HashMap<>();
        allMajorMap.putAll(majorIdMap);
        Set<String> allMajorNames = classifyResult.majors.stream()
                .map(d -> d.getName().trim())
                .collect(Collectors.toSet());
        if (!allMajorNames.isEmpty()) {
            List<Major> existingMajors = orgMapper.getMajorsByNames(new ArrayList<>(allMajorNames));
            Map<String, Long> existingMap = existingMajors.stream()
                    .collect(Collectors.toMap(Major::getName, Major::getId, (a, b) -> a));
            allMajorMap.putAll(existingMap);
        }

        //班级 - 过滤掉已存在的
        Set<String> dbClassNames;
        if (!classifyResult.classes.isEmpty()) {
            List<String> classNameList = classifyResult.classes.stream()
                    .map(d -> d.getName().trim())
                    .collect(Collectors.toList());
            List<ClassInfo> dbClasses = orgMapper.getClassesByNames(classNameList);
            dbClassNames = dbClasses.stream()
                    .map(ClassInfo::getName)
                    .collect(Collectors.toSet());
        } else {
            dbClassNames = new HashSet<>();
        }

        List<OrgExcelDTO> newClassList = classifyResult.classes.stream()
                .filter(d -> !dbClassNames.contains(d.getName().trim()))
                .collect(Collectors.toList());

        //校验班级（只校验新增的）
        List<String> classErrors = validateClasses(newClassList, allMajorMap);
        if (!classErrors.isEmpty()) {
            throw new BaseException("班级数据校验失败：" + String.join("；", classErrors));
        }

        List<ClassInfo> newClasses = buildClassEntities(newClassList, allMajorMap);
        int classCount = 0;
        if (!newClasses.isEmpty()) {
            classCount = orgMapper.batchInsertClasses(newClasses);
            log.info("新增班级 {} 条", classCount);
        }

        //返回结果
        int collegeCount = newColleges.size();
        int majorCount = newMajors.size();
        int total = collegeCount + majorCount + classCount;
        log.info("批量导入完成，新增学院 {} 条，专业 {} 条，班级 {} 条，总计 {} 条",
                collegeCount, majorCount, classCount, total);

        Map<String, Long> result = new HashMap<>();
        result.put("college", (long) collegeCount);
        result.put("major", (long) majorCount);
        result.put("class", (long) classCount);
        result.put("total", (long) total);

        return result;
    }

    /**
     * 组织批量导出
     *
     * @return
     */
    @Override
    public List<OrgExportVO> exportOrg() {
        List<OrgExportVO> exportList = new ArrayList<>();

        //学院
        List<College> collegeList = orgMapper.selectCollegeList(null);
        for (College college : collegeList) {
            OrgExportVO orgExportVO = new OrgExportVO();
            orgExportVO.setType("学院");
            orgExportVO.setName(college.getName().trim());
            orgExportVO.setCode(college.getCode());
            exportList.add(orgExportVO);
        }

        //专业
        List<Major> majorList = orgMapper.selectMajorList(null, null);
        for (Major major : majorList) {
            College college = orgMapper.getCollegeById(major.getCollegeId());
            OrgExportVO orgExportVO = new OrgExportVO();
            orgExportVO.setType("专业");
            orgExportVO.setName(major.getName().trim());
            orgExportVO.setCode(major.getCode());
            orgExportVO.setAffiliationName(college != null ? college.getName() : "");
            exportList.add(orgExportVO);
        }

        //班级
        List<ClassInfo> classInfoList = orgMapper.selectClassList(null, null);
        for (ClassInfo classInfo : classInfoList) {
            Major major = orgMapper.getMajorById(classInfo.getMajorId());
            OrgExportVO orgExportVO = new OrgExportVO();
            orgExportVO.setType("班级");
            orgExportVO.setName(classInfo.getName().trim());
            orgExportVO.setCode(classInfo.getCode());
            orgExportVO.setAffiliationName(major != null ? major.getName() : "");
            orgExportVO.setGradeYear(classInfo.getGradeYear());
            exportList.add(orgExportVO);
        }

        log.info("导出组织数据完成，共 {} 条", exportList.size());
        return exportList;
    }

    // ==================== 校验方法 ====================

    /**
     * 校验学院数据
     */
    private List<String> validateColleges(List<OrgExcelDTO> collegeList) {
        List<String> errors = new ArrayList<>();

        //检查编码是否重复
        Map<String, String> codeMap = new HashMap<>();
        for (OrgExcelDTO dto : collegeList) {
            if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
                String code = dto.getCode().trim();
                if (codeMap.containsKey(code)) {
                    errors.add("学院编码 \"" + code + "\" 重复（" + codeMap.get(code) + " 与 " + dto.getName() + "）");
                } else {
                    codeMap.put(code, dto.getName());
                }
            }
        }

        return errors;
    }

    /**
     * 校验专业数据
     */
    private List<String> validateMajors(List<OrgExcelDTO> majorList, Map<String, Long> collegeIdMap) {
        List<String> errors = new ArrayList<>();

        //检查所属学院是否存在
        for (OrgExcelDTO dto : majorList) {
            String affiliation = dto.getAffiliationName().trim();
            if (!collegeIdMap.containsKey(affiliation)) {
                errors.add("专业 \"" + dto.getName() + "\" 所属学院 \"" + affiliation + "\" 不存在");
            }
        }

        //检查编码是否重复
        Map<String, String> codeMap = new HashMap<>();
        for (OrgExcelDTO dto : majorList) {
            if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
                String code = dto.getCode().trim();
                if (codeMap.containsKey(code)) {
                    errors.add("专业编码 \"" + code + "\" 重复（" + codeMap.get(code) + " 与 " + dto.getName() + "）");
                } else {
                    codeMap.put(code, dto.getName());
                }
            }
        }

        return errors;
    }

    /**
     * 校验班级数据
     */
    private List<String> validateClasses(List<OrgExcelDTO> classList, Map<String, Long> majorIdMap) {
        List<String> errors = new ArrayList<>();

        //检查所属专业是否存在
        for (OrgExcelDTO dto : classList) {
            String affiliation = dto.getAffiliationName().trim();
            if (!majorIdMap.containsKey(affiliation)) {
                errors.add("班级 \"" + dto.getName() + "\" 所属专业 \"" + affiliation + "\" 不存在");
            }
        }

        //检查年级是否为空
        for (OrgExcelDTO dto : classList) {
            if (dto.getGradeYear() == null) {
                errors.add("班级 \"" + dto.getName() + "\" 的年级不能为空");
            }
        }

        //检查编码是否重复
        Map<String, String> codeMap = new HashMap<>();
        for (OrgExcelDTO dto : classList) {
            if (dto.getCode() != null && !dto.getCode().trim().isEmpty()) {
                String code = dto.getCode().trim();
                if (codeMap.containsKey(code)) {
                    errors.add("班级编码 \"" + code + "\" 重复（" + codeMap.get(code) + " 与 " + dto.getName() + "）");
                } else {
                    codeMap.put(code, dto.getName());
                }
            }
        }

        return errors;
    }

    // ==================== 构建实体方法 ====================

    /**
     * 构建学院实体
     */
    private List<College> buildCollegeEntities(List<OrgExcelDTO> collegeList) {
        List<College> colleges = new ArrayList<>();
        for (OrgExcelDTO dto : collegeList) {
            College college = new College();
            college.setName(dto.getName().trim());
            college.setCode(dto.getCode() != null ? dto.getCode().trim() : generateCode("college", dto.getName()));
            college.setSort(0);
            colleges.add(college);
        }
        return colleges;
    }

    /**
     * 构建专业实体
     */
    private List<Major> buildMajorEntities(List<OrgExcelDTO> majorList, Map<String, Long> collegeIdMap) {
        List<Major> majors = new ArrayList<>();
        for (OrgExcelDTO dto : majorList) {
            Major major = new Major();
            major.setName(dto.getName().trim());
            major.setCollegeId(collegeIdMap.get(dto.getAffiliationName().trim()));
            major.setCode(dto.getCode() != null ? dto.getCode().trim() : generateCode("major", dto.getName()));
            major.setSort(0);
            majors.add(major);
        }
        return majors;
    }

    /**
     * 构建班级实体
     */
    private List<ClassInfo> buildClassEntities(List<OrgExcelDTO> classList, Map<String, Long> majorIdMap) {
        List<ClassInfo> classes = new ArrayList<>();
        for (OrgExcelDTO dto : classList) {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setName(dto.getName().trim());
            classInfo.setMajorId(majorIdMap.get(dto.getAffiliationName().trim()));
            classInfo.setCode(dto.getCode() != null ? dto.getCode().trim() : generateCode("class", dto.getName()));
            classInfo.setGradeYear(dto.getGradeYear() != null ? dto.getGradeYear() : 2024);
            classInfo.setSort(0);
            classes.add(classInfo);
        }
        return classes;
    }

    /**
     * 生成编码
     */
    private String generateCode(String prefix, String name) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
