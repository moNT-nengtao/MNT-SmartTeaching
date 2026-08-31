package com.smartteaching.controller.notice;

import com.smartteaching.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName NoticeController
 * @Description
 * @Author MNT
 * @Date 2026/8/29 21:13
 **/
@RestController
@RequestMapping("/api/notice")
@Slf4j
public class NoticeController {

    /**
     * 未读公告数量
     * @return
     */
    @GetMapping("/unreadCount")
    public Result unreadCount(){
        return Result.success();
    }

}
