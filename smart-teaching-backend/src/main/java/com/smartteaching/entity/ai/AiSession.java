package com.smartteaching.entity.ai;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteaching.entity.BaseEntity;
import lombok.Data;

/**
 * @ClassName AiSession
 * @Description AI会话实体类，对应ai_session表，管理用户与AI的对话会话
 * @Author MNT
 * @Date 2026/8/14 15:39
 **/
@Data
@TableName("ai_session")
public class AiSession extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String modelName;
}
