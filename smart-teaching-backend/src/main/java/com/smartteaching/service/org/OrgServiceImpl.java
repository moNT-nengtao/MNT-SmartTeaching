package com.smartteaching.service.org;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.smartteaching.common.dto.OrgDTO;
import com.smartteaching.common.exception.BaseException;
import com.smartteaching.common.vo.OrgClassVO;
import com.smartteaching.common.vo.OrgCollegeListVO;
import com.smartteaching.common.vo.OrgMajorVO;
import com.smartteaching.common.vo.OrgTreeVO;
import com.smartteaching.entity.org.ClassInfo;
import com.smartteaching.entity.org.College;
import com.smartteaching.entity.org.Major;
import com.smartteaching.entity.user.User;
import com.smartteaching.mapper.OrgMapper;
import com.smartteaching.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
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
     * @param collegeId 学院id，可为null
     * @param isSelect
     * @return 专业VO集合
     */
    @Override
    public List<OrgMajorVO> getOrgMajorList(Long collegeId, boolean isSelect) {
        Integer status = isSelect ? 1 : null;
        List<Major> entityList = orgMapper.selectMajorList(collegeId, status);

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
        List<Major> majorList =orgMapper.selectMajorList(null, null);
        Map<Long,List<Major>> majorByCollegeIdMap = majorList.stream().collect(Collectors.groupingBy(Major::getCollegeId));

        //班级
        List<ClassInfo> classInfoList = orgMapper.selectClassList(null, null);
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
     * @param orgDTO
     */
    @Override
    public void save(OrgDTO orgDTO) {
        int rows;
        if ("college".equals(orgDTO.getType())) {
            College college = new College();
            college.setName(orgDTO.getName());
            college.setCode(orgDTO.getCode());
            college.setParentId(orgDTO.getParentId());
            rows = orgMapper.saveCollege(college);
        } else if ("major".equals(orgDTO.getType())) {
            Major major = new Major();
            major.setName(orgDTO.getName());
            major.setCode(orgDTO.getCode());
            major.setCollegeId(orgDTO.getParentId());
            rows = orgMapper.saveMajor(major);
        } else if ("class".equals(orgDTO.getType())) {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setCode(orgDTO.getCode());
            classInfo.setName(orgDTO.getName());
            classInfo.setGradeYear(orgDTO.getGradeYear());
            classInfo.setMajorId(orgDTO.getParentId());
            rows = orgMapper.saveClass(classInfo);
        } else {
            throw new RuntimeException("不支持的组织节点类型");
        }
        if (rows <= 0) {
            throw new RuntimeException("新增组织节点失败");
        }
    }

    /**
     * 编辑组织节点
     * @param orgDTO
     */
    @Override
    public void update(OrgDTO orgDTO) {
        int rows;
        if ("college".equals(orgDTO.getType())) {
            College college = new College();
            college.setId(orgDTO.getId());
            college.setName(orgDTO.getName());
            college.setCode(orgDTO.getCode());
            college.setParentId(orgDTO.getParentId());
            rows = orgMapper.updateCollege(college);
        } else if ("major".equals(orgDTO.getType())) {
            Major major = new Major();
            major.setId(orgDTO.getId());
            major.setName(orgDTO.getName());
            major.setCode(orgDTO.getCode());
            rows = orgMapper.updateMajor(major);
        } else if ("class".equals(orgDTO.getType())) {
            ClassInfo classInfo = new ClassInfo();
            classInfo.setId(orgDTO.getId());
            classInfo.setCode(orgDTO.getCode());
            classInfo.setName(orgDTO.getName());
            classInfo.setGradeYear(orgDTO.getGradeYear());
            classInfo.setMajorId(orgDTO.getParentId());
            rows = orgMapper.updateClass(classInfo);
        } else {
            throw new RuntimeException("不支持的组织节点类型");
        }
        if (rows <= 0) {
            throw new RuntimeException("编辑组织节点失败");
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
            throw new BaseException("该组织不存在");
        }

        Long count;
        switch (type) {
            case "college":
                count = orgMapper.countMajorsByCollegeId(id);
                if (count != null && count > 0) {
                    throw new BaseException("该学院下存在专业，禁止删除，请先删除下属专业");
                }
                orgMapper.deleteCollegeById(id);
                break;
            case "major":
                count = orgMapper.countClassesByMajorId(id);
                if (count != null && count > 0) {
                    throw new BaseException("该专业下存在班级，禁止删除，请先删除下属班级");
                }
                orgMapper.deleteMajorById(id);
                break;
            case "class":
                count = orgMapper.countStudentsByClassId(id);
                if (count != null && count > 0) {
                    throw new BaseException("该班级下存在学生，禁止删除，请先移除该班级下的学生");
                }
                orgMapper.deleteClassById(id);
                break;
            default:
                throw new BaseException("非法节点类型: " + type);
        }
    }

    /**
     * 学生移出班级
     * @param ids
     */
    @Override
    public void studentsRemove(String ids) {
        if (ids == null) {
            throw new BaseException("未选择学生");
        }

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
}


