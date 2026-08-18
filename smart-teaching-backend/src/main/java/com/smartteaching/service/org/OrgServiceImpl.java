package com.smartteaching.service.org;

import com.smartteaching.common.vo.OrgClassVO;
import com.smartteaching.common.vo.OrgCollegeListVO;
import com.smartteaching.common.vo.OrgMajorVO;
import com.smartteaching.entity.org.ClassInfo;
import com.smartteaching.entity.org.College;
import com.smartteaching.entity.org.Major;
import com.smartteaching.mapper.OrgMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrgServiceImpl implements OrgService {

    @Autowired
    private OrgMapper orgMapper;

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
}
