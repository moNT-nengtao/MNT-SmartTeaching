package com.smartteaching.controller.org;

import com.smartteaching.common.result.Result;
import com.smartteaching.common.vo.OrgClassVO;
import com.smartteaching.common.vo.OrgCollegeListVO;
import com.smartteaching.common.vo.OrgMajorVO;
import com.smartteaching.service.org.OrgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/org")
public class OrgController {

    @Autowired
    private OrgService orgService;

    /**
     * 学院列表 (传isSelect检查前端是不是下拉菜单，是则屏蔽禁用内容)
     * @return
     */
    @GetMapping("/college/list")
    public Result<List<OrgCollegeListVO>> getCollegeList(
            @RequestParam(defaultValue = "false") boolean isSelect
    ) {
        List<OrgCollegeListVO> collegeList = orgService.getOrgCollegeList(isSelect);
        return Result.success(collegeList);
    }


    /**
     * 获取专业列表
     * @param collegeId
     * @return
     */
    //接收学院id才能找对应专业列表
    @GetMapping("/major/list")
    public Result<List<OrgMajorVO>> getMajorList(@RequestParam(required = false) Long collegeId,
                                                 @RequestParam(defaultValue = "false") boolean isSelect
    ){
        log.info("专业列表，接收学院id:{}", collegeId);

        List<OrgMajorVO> majorList = orgService.getOrgMajorList(collegeId, isSelect);

        return Result.success(majorList);
    }


    /**
     * 获取班级列表
     * @param majorId
     * @return
     */
    @GetMapping("/class/list")
    public Result<List<OrgClassVO>> getClassList(@RequestParam(required = false) Long majorId,
                                                 @RequestParam(defaultValue = "false") boolean isSelect
    ){
        log.info("班级列表，接收专业id:{}", majorId);

        List<OrgClassVO> classList = orgService.getOrgClassList(majorId, isSelect);

        return Result.success(classList);
    }

}
