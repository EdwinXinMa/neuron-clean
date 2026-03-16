package com.echarge.modules.openapi.controller;

import com.echarge.common.api.vo.Result;
import com.echarge.common.system.base.controller.NeuronController;
import com.echarge.modules.openapi.entity.OpenApiPermission;
import com.echarge.modules.openapi.service.OpenApiPermissionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/openapi/permission")
public class OpenApiPermissionController extends NeuronController<OpenApiPermission, OpenApiPermissionService> {

    @PostMapping("add")
    public Result add(@RequestBody OpenApiPermission openApiPermission) {
        service.add(openApiPermission);
        return Result.ok("保存成功");
    }
    @GetMapping("/getOpenApi")
    public Result<?> getOpenApi( String apiAuthId) {
        return service.getOpenApi(apiAuthId);
    }
}
