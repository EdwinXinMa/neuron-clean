package com.echarge.modules.system.service.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import com.baomidou.mybatisplus.annotation.DbType;
import com.echarge.common.system.api.ISysBaseApi;
import com.echarge.common.util.CommonUtils;
import com.echarge.modules.system.entity.SysLog;
import com.echarge.modules.system.mapper.SysLogMapper;
import com.echarge.modules.system.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * <p>
 * 系统日志表 服务实现类
 * </p>
 *
 * @Author Edwin
 * @since 2026-03-22
 * @author Edwin
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements ISysLogService {

	@Resource
	private SysLogMapper sysLogMapper;
	
	/**
	 * @功能：清空所有日志记录
	 */
	@Override
	public void removeAll() {
		sysLogMapper.removeAll();
	}

	/** {@inheritDoc} */
	@Override
	public Long findTotalVisitCount() {
		return sysLogMapper.findTotalVisitCount();
	}

	/** {@inheritDoc} */
	@Override
	public Long findTodayVisitCount(Date dayStart, Date dayEnd) {
		return sysLogMapper.findTodayVisitCount(dayStart,dayEnd);
	}

	/** {@inheritDoc} */
	@Override
	public Long findTodayIp(Date dayStart, Date dayEnd) {
		return sysLogMapper.findTodayIp(dayStart,dayEnd);
	}

	/** {@inheritDoc} */
	@Override
	public List<Map<String,Object>> findVisitCount(Date dayStart, Date dayEnd) {
		DbType dbType = CommonUtils.getDatabaseTypeEnum();
		return sysLogMapper.findVisitCount(dayStart, dayEnd,dbType.getDb());
	}
}
