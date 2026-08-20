package com.smartteaching.controller.org;

import com.smartteaching.common.dto.OrgDTO;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.OrgClassVO;
import com.smartteaching.common.vo.OrgCollegeListVO;
import com.smartteaching.common.vo.OrgMajorVO;
import com.smartteaching.common.vo.OrgTreeVO;
import com.smartteaching.service.org.OrgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/org")
public class OrgController {

    @Autowired
    private OrgService orgService;

    @Autowired
    private JwtUtil jwtUtil;

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


    /**
     * 组织树
     * @return
     */
    @GetMapping("/tree")
    public Result<List<OrgTreeVO>> orgTree(){
        log.info("组织树");

        List<OrgTreeVO> orgTreeVOS = orgService.bulidOrgTree();
        return Result.success(orgTreeVOS);
    }

    /**
     * 新增组织节点
     * @param orgDTO
     * @return
     */
    @PostMapping
    public Result save(@Validated({OrgDTO.Group.Add.class}) @RequestBody OrgDTO orgDTO){
        log.info("新增组织节点:{}", orgDTO);
        orgService.save(orgDTO);
        return Result.success();
    }

    /**
     * 编辑组织节点
     * @param orgDTO
     * @return
     */
    @PutMapping
    public Result update(@Validated(OrgDTO.Group.Update.class) @RequestBody OrgDTO orgDTO){
        log.info("编辑组织节点:{}", orgDTO);

        orgService.update(orgDTO);
        return Result.success();
    }

    /**
     * 删除组织节点(软删除)
     * @return
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id,
                         @RequestParam String type){
        log.info("删除组织节点:{},{}",id,type);
        orgService.delete(id,type);
        return Result.success();
    }

    /**
     * 学生移出班级(禁用)
     * @param body
     * @return
     */
    @PostMapping("/students/remove")
    public Result removeStudents(@RequestBody Map<String,Object> body){
        log.info("学生移出班级");
        //前端传过来的是一个完整JSON字符串，需求取出其中的ids
        Object obj = body.get("ids");
        String ids;
        if(obj instanceof List<?> list){
            ids = list.stream().map(Object::toString).collect(Collectors.joining(","));
        }else{
            ids = (String) obj;
        }

        log.info("学生移出班级");
        orgService.studentsRemove(ids);
        return Result.success();
    }


    /**
     * 组织批量导入
     * @param file
     * @return
     */
    @PostMapping("/batchImport")
    public Result batchImportOrg(@RequestParam("files") MultipartFile file){
        if (file == null) {
            log.warn("批量导入失败:文件为空");
            return  Result.error("文件不能为空，请选择要上传的 Excel 文件");
        }

        log.info("组织批量导入:{}", file.getOriginalFilename());
        Map<String,Object> successCount = orgService.batchImportOrg(file);
        return Result.success(successCount);
    }


}
