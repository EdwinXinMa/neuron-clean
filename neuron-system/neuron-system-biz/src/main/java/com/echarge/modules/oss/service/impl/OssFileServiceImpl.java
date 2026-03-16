package com.echarge.modules.oss.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.common.util.CommonUtils;
import com.echarge.common.util.oConvertUtils;
import com.echarge.common.util.oss.OssBootUtil;
import com.echarge.modules.oss.entity.OssFile;
import com.echarge.modules.oss.mapper.OssFileMapper;
import com.echarge.modules.oss.service.IOssFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Description: OSS云存储实现类
 * @author: jeecg-boot
 */
@Service("ossFileService")
public class OssFileServiceImpl extends ServiceImpl<OssFileMapper, OssFile> implements IOssFileService {

	@Override
	public void upload(MultipartFile multipartFile) throws Exception {
		String fileName = multipartFile.getOriginalFilename();
		fileName = CommonUtils.getFileName(fileName);
		OssFile ossFile = new OssFile();
		ossFile.setFileName(fileName);
		String url = OssBootUtil.upload(multipartFile,"upload/test");
		if(oConvertUtils.isEmpty(url)){
			throw new NeuronBootException("上传文件失败! ");
		}
		// 返回阿里云原生域名前缀URL
		ossFile.setUrl(OssBootUtil.getOriginalUrl(url));
		this.save(ossFile);
	}

	@Override
	public boolean delete(OssFile ossFile) {
		try {
			this.removeById(ossFile.getId());
			OssBootUtil.deleteUrl(ossFile.getUrl());
		}
		catch (Exception ex) {
			log.error(ex.getMessage(),ex);
			return false;
		}
		return true;
	}

}
