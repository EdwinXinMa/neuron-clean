package com.echarge.modules.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.system.query.QueryGenerator;
import com.echarge.modules.device.entity.Device;
import com.echarge.modules.device.service.IDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "设备管理")
@RestController
@RequestMapping("/device")
public class DeviceController {

    @Autowired
    private IDeviceService deviceService;

    @Operation(summary = "分页查询设备列表")
    @GetMapping("/list")
    public Result<IPage<Device>> list(Device device,
                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                       HttpServletRequest req) {
        QueryWrapper<Device> queryWrapper = QueryGenerator.initQueryWrapper(device, req.getParameterMap());
        Page<Device> page = new Page<>(pageNo, pageSize);
        IPage<Device> pageList = deviceService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加设备")
    @PostMapping("/add")
    public Result<String> add(@RequestBody Device device) {
        deviceService.save(device);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑设备")
    @PutMapping("/edit")
    public Result<String> edit(@RequestBody Device device) {
        deviceService.updateById(device);
        return Result.OK("编辑成功!");
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String id) {
        deviceService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "根据ID查询设备")
    @GetMapping("/queryById")
    public Result<Device> queryById(@RequestParam String id) {
        Device device = deviceService.getById(id);
        return Result.OK(device);
    }
}
