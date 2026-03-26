package com.echarge.modules.system.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.echarge.modules.system.entity.SysLog;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统日志表 服务类
 * @author Edwin
 * @since 2026-03-22
 */
public interface ISysLogService extends IService<SysLog> {

	/**
	 * 清空所有日志记录
	 */
	public void removeAll();

	/**
	 * 获取系统总访问次数
	 * @return 总访问次数
	 */
	Long findTotalVisitCount();

	/**
	 * 获取系统今日访问次数
	 * @param dayStart 开始时间
	 * @param dayEnd   结束时间
	 * @return 今日访问次数
	 */
	Long findTodayVisitCount(Date dayStart, Date dayEnd);

	/**
	 * 获取系统今日访问IP数
	 * @param dayStart 开始时间
	 * @param dayEnd   结束时间
	 * @return 今日访问IP数
	 */
	Long findTodayIp(Date dayStart, Date dayEnd);

	/**
	 * 首页：根据时间统计访问数量/IP数量
	 * @param dayStart 开始时间
	 * @param dayEnd   结束时间
	 * @return 访问统计列表
	 */
	List<Map<String,Object>> findVisitCount(Date dayStart, Date dayEnd);
}
