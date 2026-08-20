package com.smartteaching.service.org;

import com.smartteaching.common.dto.OrgDTO;
import com.smartteaching.common.vo.OrgClassVO;
import com.smartteaching.common.vo.OrgCollegeListVO;
import com.smartteaching.common.vo.OrgMajorVO;
import com.smartteaching.common.vo.OrgTreeVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    /**
     * 组织树
     * @return
     */
    List<OrgTreeVO> bulidOrgTree();

    /**
     * 新增组织节点
     * @param orgDTO
     */
    void save(OrgDTO orgDTO);

    /**
     * 编辑组织节点
     * @param orgDTO
     */
    void update(OrgDTO orgDTO);

    /**
     * 删除组织节点
     *
     * @param id
     * @param type
     */
    void delete(Long id, String type);

    /**
     * 学生移出班级
     * @param ids
     */
    void studentsRemove(String ids);

    /**
     * 组织批量导入
     *
     * @param file
     * @return
     */
    Map<String, Object> batchImportOrg(MultipartFile file);
}
