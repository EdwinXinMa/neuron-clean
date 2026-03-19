package com.echarge.modules.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.echarge.common.api.vo.Result;
import com.echarge.common.system.query.QueryGenerator;
import com.echarge.modules.device.entity.NcConnector;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.service.INcConnectorService;
import com.echarge.modules.device.service.INcDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "设备台账")
@RestController
@RequestMapping("/device/ncDevice")
public class NcDeviceController {

    @Autowired
    private INcDeviceService ncDeviceService;

    @Autowired
    private INcConnectorService ncConnectorService;

    @Operation(summary = "分页查询")
    @GetMapping("/list")
    public Result<IPage<NcDevice>> list(NcDevice ncDevice,
                                        @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                        @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                        HttpServletRequest req) {
        QueryWrapper<NcDevice> queryWrapper = QueryGenerator.initQueryWrapper(ncDevice, req.getParameterMap());
        Page<NcDevice> page = new Page<>(pageNo, pageSize);
        IPage<NcDevice> pageList = ncDeviceService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    @Operation(summary = "添加")
    @PostMapping("/add")
    public Result<String> add(@RequestBody NcDevice ncDevice) {
        ncDeviceService.save(ncDevice);
        return Result.OK("添加成功！");
    }

    @Operation(summary = "编辑")
    @RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
    public Result<String> edit(@RequestBody NcDevice ncDevice) {
        ncDeviceService.updateById(ncDevice);
        return Result.OK("编辑成功!");
    }

    @Operation(summary = "删除")
    @DeleteMapping("/delete")
    public Result<String> delete(@RequestParam String id) {
        ncDeviceService.removeById(id);
        return Result.OK("删除成功!");
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<String> deleteBatch(@RequestParam String ids) {
        ncDeviceService.removeByIds(Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功!");
    }

    @Operation(summary = "根据ID查询")
    @GetMapping("/queryById")
    public Result<NcDevice> queryById(@RequestParam String id) {
        NcDevice ncDevice = ncDeviceService.getById(id);
        if (ncDevice == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(ncDevice);
    }

    @Operation(summary = "查询下挂子设备（桩+枪）")
    @GetMapping("/children")
    public Result<List<Map<String, Object>>> children(@RequestParam String parentId) {
        // 查桩
        List<NcDevice> piles = ncDeviceService.list(
                new QueryWrapper<NcDevice>().eq("parent_device_id", parentId)
        );
        // 查所有桩下的枪
        List<String> pileIds = piles.stream().map(NcDevice::getId).collect(Collectors.toList());
        List<NcConnector> allConnectors = pileIds.isEmpty() ? Collections.emptyList()
                : ncConnectorService.list(new QueryWrapper<NcConnector>().in("device_id", pileIds));
        // 按桩ID分组
        Map<String, List<NcConnector>> connectorMap = allConnectors.stream()
                .collect(Collectors.groupingBy(NcConnector::getDeviceId));
        // 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (NcDevice pile : piles) {
            Map<String, Object> pileMap = new HashMap<>();
            pileMap.put("device", pile);
            pileMap.put("connectors", connectorMap.getOrDefault(pile.getId(), Collections.emptyList()));
            result.add(pileMap);
        }
        return Result.OK(result);
    }

    @Operation(summary = "导出Excel")
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, NcDevice ncDevice) {
        QueryWrapper<NcDevice> queryWrapper = QueryGenerator.initQueryWrapper(ncDevice, request.getParameterMap());
        List<NcDevice> list = ncDeviceService.list(queryWrapper);
        ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
        mv.addObject(NormalExcelConstants.FILE_NAME, "设备台账");
        mv.addObject(NormalExcelConstants.CLASS, NcDevice.class);
        mv.addObject(NormalExcelConstants.PARAMS, new ExportParams("设备台账数据", "导出信息"));
        mv.addObject(NormalExcelConstants.DATA_LIST, list);
        return mv;
    }

    @Operation(summary = "导入Excel")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile file = entity.getValue();
            ImportParams params = new ImportParams();
            params.setTitleRows(2);
            params.setHeadRows(1);
            params.setNeedSave(true);
            try {
                List<NcDevice> list = ExcelImportUtil.importExcel(file.getInputStream(), NcDevice.class, params);
                ncDeviceService.saveBatch(list);
                return Result.OK("文件导入成功！数据行数：" + list.size());
            } catch (Exception e) {
                log.error("导入失败：" + e.getMessage(), e);
                return Result.error("文件导入失败：" + e.getMessage());
            }
        }
        return Result.error("文件导入失败！");
    }
}
