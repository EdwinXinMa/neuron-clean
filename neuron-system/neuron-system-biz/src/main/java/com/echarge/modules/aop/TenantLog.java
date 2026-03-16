package com.echarge.modules.aop;

import com.echarge.common.constant.CommonConstant;
import com.echarge.common.constant.enums.ModuleType;

import java.lang.annotation.*;

/**
 * 系统日志注解
 * 
 * @Author scott
 * @email jeecgos@163.com
 * @Date 2019年1月14日
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantLog {

	/**
	 * 操作日志类型（1查询，2添加，3修改，4删除）
	 * 
	 * @return
	 */
	int value() default 0;

}
