package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.UserQueryDTO;
import com.smartteaching.common.vo.UserQueryVO;
import com.smartteaching.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 用户列表
     * @param mpPage
     * @param userQueryDTO
     * @return
     */
    IPage<UserQueryVO> selectUserPage(IPage<UserQueryVO> mpPage, @Param("dto") UserQueryDTO userQueryDTO);
}
