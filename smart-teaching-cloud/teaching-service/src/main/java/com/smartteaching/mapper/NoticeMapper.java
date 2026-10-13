package com.smartteaching.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteaching.common.dto.notice.NoticeQueryDTO;
import com.smartteaching.common.vo.notice.NoticeListVO;
import com.smartteaching.entity.notice.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 分页查询当前用户可见的公告（按角色/选课范围过滤）
     */
    IPage<NoticeListVO> selectNoticePage(IPage<NoticeListVO> page,
                                         @Param("dto") NoticeQueryDTO dto,
                                         @Param("userId") Long userId,
                                         @Param("role") String role);

    /**
     * 查询单条当前用户可见的公告
     */
    NoticeListVO selectNoticeDetail(@Param("id") Long id,
                                    @Param("userId") Long userId,
                                    @Param("role") String role);

    /**
     * 统计当前用户可见且未读的公告数
     */
    Long countUnread(@Param("userId") Long userId, @Param("role") String role);
}
