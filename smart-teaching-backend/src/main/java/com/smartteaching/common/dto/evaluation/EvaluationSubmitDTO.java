package com.smartteaching.common.dto.evaluation;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * @ClassName EvaluationSubmitDTO
 * @Description
 * @Author MNT
 * @Date 2026/8/30 22:13
 **/
@Data
public class EvaluationSubmitDTO {

    /**
     * 课程ID
     */
    @NotNull(message = "课程ID不能为空")
    private Long courseId;

    /**
     * 授课能力评分（1-5分）
     */
    @NotNull(message = "请对授课能力进行评分")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能高于5分")
    private Double teachingAbility;

    /**
     * 课堂氛围评分（1-5分）
     */
    @NotNull(message = "请对课堂氛围进行评分")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能高于5分")
    private Double classAtmosphere;

    /**
     * 知识讲解清晰度评分（1-5分）
     */
    @NotNull(message = "请对知识讲解清晰度进行评分")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能高于5分")
    private Double knowledgeClarity;

    /**
     * 作业批改反馈评分（1-5分）
     */
    @NotNull(message = "请对作业批改反馈进行评分")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能高于5分")
    private Double homeworkFeedback;

    /**
     * 答疑服务评分（1-5分）
     */
    @NotNull(message = "请对答疑服务进行评分")
    @Min(value = 1, message = "评分不能低于1分")
    @Max(value = 5, message = "评分不能高于5分")
    private Double qaService;

    /**
     * 文字评价内容
     */
    @NotBlank(message = "请填写文字评价")
    @Size(max = 500, message = "评价内容不能超过500字")
    private String comment;

    /**
     * 是否匿名
     */
    private Boolean isAnonymous;
}