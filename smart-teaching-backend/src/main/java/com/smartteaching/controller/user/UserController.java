package com.smartteaching.controller.user;

import com.smartteaching.common.dto.UserAddDTO;
import com.smartteaching.common.dto.UserQueryDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.common.result.Result;
import com.smartteaching.common.utils.JwtUtil;
import com.smartteaching.common.vo.UserQueryVO;
import com.smartteaching.entity.user.User;
import com.smartteaching.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     * @param userAddDTO
     * @param authHeader
     * @param httpRequest
     * @return
     */
    @PostMapping
    public Result save(@Valid UserAddDTO userAddDTO,
                       @RequestPart(required = false) MultipartFile avatarFile,
                       @RequestHeader(value = "Authorization", required = true) String authHeader,
                       HttpServletRequest httpRequest){

        log.info("新增用户：{}", userAddDTO);

        jwtUtil.extractAndValidateToken(authHeader);

        userService.addUser(userAddDTO, avatarFile);

        return Result.success("新增用户成功");
    }




}
