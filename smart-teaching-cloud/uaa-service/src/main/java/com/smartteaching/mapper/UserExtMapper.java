package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.user.UserQueryDTO;
import com.smartteaching.common.vo.user.UserQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * uaa 用户扩展 Mapper：继承公共 UserMapper，仅保留 uaa 独有的用户分页查询
 */
@Mapper
public interface UserExtMapper extends UserMapper {

    /**
     * 用户列表
     * @param mpPage
     * @param userQueryDTO
     * @return
     */
    IPage<UserQueryVO> selectUserPage(IPage<UserQueryVO> mpPage, @Param("dto") UserQueryDTO userQueryDTO);
}
