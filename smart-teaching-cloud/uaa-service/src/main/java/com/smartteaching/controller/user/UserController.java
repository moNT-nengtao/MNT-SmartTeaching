package com.smartteaching.controller.user;

import com.smartteaching.common.dto.user.UserSaveDTO;
import com.smartteaching.common.dto.user.UserQueryDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.user.UserQueryVO;
import com.smartteaching.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @ClassName UserController
 * @Description 用户管理控制器，提供用户列表、新增、编辑、删除、启用/禁用及批量导入接口
 * @Author MNT
 * @Date 2026/8/15 09:52
 **/
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户列表、分页搜索
     * @param userQueryDTO
     * @return
     */
    @GetMapping("/list")
    public Result<PageResult<UserQueryVO>> getUsersList(UserQueryDTO userQueryDTO) {
        log.info("用户列表、分页搜索:{}", userQueryDTO);

        PageResult<UserQueryVO> pageResult = userService.pageQuery(userQueryDTO);

        return Result.success(pageResult);
    }


    /**
     * 新增用户
     * @param userSaveDTO
     * @param httpRequest
     * @return
     */
    //普通字段塞进 FormData，头像文件单独 append `avatarFile`
    @PostMapping
    public Result save(@Validated(UserSaveDTO.AddGroup.class) UserSaveDTO userSaveDTO,
                       @RequestPart(required = false) MultipartFile avatarFile,
                       HttpServletRequest httpRequest){

        log.info("新增用户：{}", userSaveDTO);

        userService.addUser(userSaveDTO, avatarFile);

        return Result.success("新增用户成功");
    }


    /**
     * 编辑用户
     * @param userSaveDTO
     * @return
     */
    @PutMapping
    public Result updateUser(@Validated(UserSaveDTO.UpdateGroup.class) UserSaveDTO userSaveDTO,
                             @RequestPart(required = false) MultipartFile avatarFile,
                             HttpServletRequest httpRequest){

        log.info("编辑用户:{}",userSaveDTO);

        userService.updateUser(userSaveDTO,avatarFile);

        return Result.success();
    }


    /**
     * 删除用户(硬删除！)
     * @return
     */
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable String id,
                         @RequestHeader(value = "Authorization", required = true) String authHeader){
        log.info("删除用户:{}",id);

        jwtUtil.extractAndValidateToken(authHeader);
        Long loginUserId = jwtUtil.getUserIdFromHeader(authHeader);

        userService.deleteUser(id,loginUserId);

        return Result.success();
    }

    /**
     * 启用/禁用用户
     */
    @PutMapping("/{id}/status")
    public Result statusUser(@PathVariable String id,
                             @RequestHeader(value = "Authorization", required = true) String authHeader){
        log.info("启用/禁用用户:{}",id);

        jwtUtil.extractAndValidateToken(authHeader);
        Long loginUserId = jwtUtil.getUserIdFromHeader(authHeader);

        userService.statusUser(id,loginUserId);

        return Result.success();
    }


    /**
     * 批量导入用户
     * @param file
     * @return
     */
    @PostMapping("/batchImport")
    public Result batchImportUser(@RequestParam("file") MultipartFile file){

        if(file.isEmpty()){
            log.warn("批量导入失败:文件为空");
            return  Result.error("文件不能为空，请选择要上传的 Excel 文件");
        }

        log.info("批量导入用户，文件名: {}", file.getOriginalFilename());

        int successCount = userService.batchImportUsers(file);
        return Result.success("批量导入成功，共导入 " + successCount + " 条数据");

    }



}
