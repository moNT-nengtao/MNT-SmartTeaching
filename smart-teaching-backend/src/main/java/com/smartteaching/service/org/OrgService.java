package com.smartteaching.service.org;

import com.smartteaching.common.vo.OrgClassVO;
import com.smartteaching.common.vo.OrgCollegeListVO;
import com.smartteaching.common.vo.OrgMajorVO;

import java.util.List;

public interface OrgService {

    /**
     * 学院列表
     * @return
     */
    List<OrgCollegeListVO> getOrgCollegeList(boolean isSelect);

    /**
     * 获取专业列表
     * @param collegeId 学院id，可为null
     * @param isSelect
     * @return
     */
    List<OrgMajorVO> getOrgMajorList(Long collegeId, boolean isSelect);

    /**
     * 获取班级列表
     * @param majorId 专业id，可为null
     * @param isSelect
     * @return
     */
    List<OrgClassVO> getOrgClassList(Long majorId, boolean isSelect);
}
