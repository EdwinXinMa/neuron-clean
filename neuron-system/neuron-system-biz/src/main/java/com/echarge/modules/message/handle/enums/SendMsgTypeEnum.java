package com.echarge.modules.message.handle.enums;

import com.echarge.common.util.oConvertUtils;

/**
 * 发送消息类型枚举
 * @author: jeecg-boot
 */
public enum SendMsgTypeEnum {

    /**
     * 短信
     */
	SMS("1", "com.echarge.modules.message.handle.impl.SmsSendMsgHandle"),
    /**
     * 邮件
     */
	EMAIL("2", "com.echarge.modules.message.handle.impl.EmailSendMsgHandle"),
    /**
     * 系统消息
     */
	SYSTEM_MESSAGE("4","com.echarge.modules.message.handle.impl.SystemSendMsgHandle");

	private String type;

	private String implClass;

	private SendMsgTypeEnum(String type, String implClass) {
		this.type = type;
		this.implClass = implClass;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getImplClass() {
		return implClass;
	}

	public void setImplClass(String implClass) {
		this.implClass = implClass;
	}

	public static SendMsgTypeEnum getByType(String type) {
		if (oConvertUtils.isEmpty(type)) {
			return null;
		}
		for (SendMsgTypeEnum val : values()) {
			if (val.getType().equals(type)) {
				return val;
			}
		}
		return null;
	}
}
