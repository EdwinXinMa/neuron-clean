package com.echarge.common.constant;

/**
 * 通用常量（精简版 - 仅保留业务实际使用的常量）
 * @author Edwin
 */
public interface CommonConstant {

	// ======================== 状态标记 ========================
	Integer DEL_FLAG_1 = 1;
	Integer DEL_FLAG_0 = 0;

	// ======================== 用户状态 ========================
	/** 1正常(解冻) 2冻结 */
	Integer USER_UNFREEZE = 1;
	Integer USER_FREEZE = 2;

	// ======================== 系统日志类型 ========================
	int LOG_TYPE_4 = 4;

	// ======================== HTTP 状态码 ========================
	Integer SC_OK_200 = 200;
	Integer SC_INTERNAL_SERVER_ERROR_500 = 500;
	/** 访问权限认证未通过 */
	Integer SC_NO_AUTHZ = 510;

	// ======================== Token / 认证 ========================
	String X_ACCESS_TOKEN = "X-Access-Token";
	String TENANT_ID = "X-Tenant-Id";
	String TOKEN_IS_INVALID_MSG = "Token失效，请重新登录!";
	String X_FORWARDED_SCHEME = "X-Forwarded-Scheme";

	/** 登录用户Shiro权限缓存KEY前缀 */
	String PREFIX_USER_SHIRO_CACHE = "shiro:cache:com.echarge.config.shiro.ShiroRealm.authorizationCache:";
	/** 登录用户Token令牌缓存KEY前缀 */
	String PREFIX_USER_TOKEN = "prefix_user_token:";
	/** Token令牌作废提示信息KEY前缀 */
	String PREFIX_USER_TOKEN_ERROR_MSG = "prefix_user_token:error:msg_";

	/** 客户端类型 */
	String CLIENT_TYPE_PC = "PC";
	String CLIENT_TYPE_APP = "APP";

	// ======================== 微服务 ========================
	String CLOUD_SERVER_KEY = "spring.cloud.nacos.discovery.server-addr";

	// ======================== 文件上传类型 ========================
	String UPLOAD_TYPE_LOCAL = "local";
	String UPLOAD_TYPE_MINIO = "minio";

	// ======================== 排序 ========================
	String ORDER_TYPE_ASC = "ASC";
	String ORDER_TYPE_DESC = "DESC";

	// ======================== 其他 ========================
	String UNKNOWN = "unknown";
	String STR_HTTP = "http";
	String STRING_NULL = "null";
	String DICT_TEXT_SUFFIX = "_dictText";
}
