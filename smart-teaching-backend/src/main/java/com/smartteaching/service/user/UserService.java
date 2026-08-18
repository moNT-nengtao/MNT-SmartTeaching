package com.smartteaching.service.user;

import com.smartteaching.common.dto.UserAddDTO;
import com.smartteaching.common.dto.UserQueryDTO;
import com.smartteaching.common.result.PageResult;
import com.smartteaching.entity.user.User;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

    /**
     * 用户列表
     * @param userQueryDTO
     * @return
     */
    PageResult pageQuery(UserQueryDTO userQueryDTO);

    /**
     * 新增用户
     *
     * @param userAddDTO
     * @param avatarFile
     * @return
     */
    void addUser(UserAddDTO userAddDTO, MultipartFile avatarFile);


}
