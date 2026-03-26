package com.echarge.modules.system.controller;

import lombok.extern.slf4j.Slf4j;
import com.echarge.common.api.vo.Result;
import com.echarge.common.util.CommonUtils;
import com.echarge.common.util.MinioUtil;
import com.echarge.common.util.filter.SsrfFileTypeFilter;
import com.echarge.common.util.OConvertUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * minio文件上传
 * @author Edwin
 */
@Slf4j
@RestController
@RequestMapping("/sys/upload")
public class SysUploadController {

    /**
     * 上传
     * @param request
     */
    @PostMapping(value = "/uploadMinio")
    public Result<?> uploadMinio(HttpServletRequest request) throws Exception {
        Result<?> result = new Result<>();
        String bizPath = request.getParameter("biz");
        String bucket = request.getParameter("bucket");
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");

        SsrfFileTypeFilter.checkUploadFileType(file, bizPath);

        if(OConvertUtils.isEmpty(bizPath)){
            bizPath = "";
        }
        String orgName = file.getOriginalFilename();
        orgName = CommonUtils.getFileName(orgName);
        String fileUrl;
        if (OConvertUtils.isNotEmpty(bucket)) {
            fileUrl = MinioUtil.upload(file, bizPath, bucket);
        } else {
            fileUrl = MinioUtil.upload(file, bizPath);
        }
        if(OConvertUtils.isEmpty(fileUrl)){
            return Result.error("上传失败,请检查配置信息是否正确!");
        }
        result.setMessage(fileUrl);
        result.setSuccess(true);
        return result;
    }
}
