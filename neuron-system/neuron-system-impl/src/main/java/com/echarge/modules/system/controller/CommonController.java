package com.echarge.modules.system.controller;

import lombok.extern.slf4j.Slf4j;
import com.echarge.common.api.vo.Result;
import org.springframework.web.bind.annotation.*;

/**
 * 通用控制器（精简版）
 */
@Slf4j
@RestController
@RequestMapping("/sys/common")
public class CommonController {

    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.OK("pong");
    }
}
