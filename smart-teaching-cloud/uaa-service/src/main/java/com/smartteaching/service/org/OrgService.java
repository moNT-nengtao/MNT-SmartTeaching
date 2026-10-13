package com.smartteaching.service.org;

import com.smartteaching.common.dto.org.OrgDTO;
import com.smartteaching.common.vo.org.OrgExportVO;
import com.smartteaching.common.vo.org.OrgClassVO;
import com.smartteaching.common.vo.org.OrgCollegeListVO;
import com.smartteaching.common.vo.org.OrgMajorVO;
import com.smartteaching.common.vo.org.OrgTreeVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @ClassName OrgService
 * @Description 组织服务接口
 * @Author MNT
 * @Date 2026/8/16 09:45
 **/
public interface OrgService {

    /**
     * 学院列表
     * @return
     */
    List<OrgCollegeListVO> getOrgCollegeList(boolean isSelect);

    /**
     * 获取专业列表
     *
     * @param collegeId 学院id，可为null
     * @return
     */
    List<OrgMajorVO> getOrgMajorList(Long collegeId);

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
    Map<String, Long> batchImportOrg(MultipartFile file);

    /**
     * 组织批量导出
     *
     * @return
     */
    List<OrgExportVO> exportOrg();
}
