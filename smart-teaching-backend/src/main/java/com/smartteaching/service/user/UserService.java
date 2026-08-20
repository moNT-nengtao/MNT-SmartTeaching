package com.smartteaching.service.user;

import com.smartteaching.common.dto.UserSaveDTO;
import com.smartteaching.common.dto.UserQueryDTO;
import com.smartteaching.common.result.PageResult;
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
     * @param userSaveDTO
     * @param avatarFile
     * @return
     */
    void addUser(UserSaveDTO userSaveDTO, MultipartFile avatarFile);


    /**
     * 编辑用户
     *
     * @param userSaveDTO
     * @param avatarFile
     */
    void updateUser(UserSaveDTO userSaveDTO, MultipartFile avatarFile);

    /**
     * 删除用户
     *
     * @param id
     * @param loginUserId
     */
    void deleteUser(String id, Long loginUserId);

    /**
     * 启用/禁用用户
     * @param id
     * @param loginUserId
     */
    void statusUser(String id, Long loginUserId);

    /**
     * 批量导入用户
     * @param file
     * @return
     */
    int batchImportUsers(MultipartFile file);
}
