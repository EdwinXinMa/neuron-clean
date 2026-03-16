package com.echarge.modules.message.handle.impl;

import lombok.extern.slf4j.Slf4j;
import com.echarge.common.api.dto.message.MessageDTO;
import com.echarge.modules.message.handle.ISendMsgHandle;
import com.echarge.modules.system.service.impl.ThirdAppWechatEnterpriseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Description: 发企业微信消息模板
 * @author: jeecg-boot
 */
@Slf4j
@Component("qywxSendMsgHandle")
public class QywxSendMsgHandle implements ISendMsgHandle {

	@Autowired
	private ThirdAppWechatEnterpriseServiceImpl wechatEnterpriseService;

	@Override
	public void sendMsg(String esReceiver, String esTitle, String esContent) {
		log.info("发微信消息模板");
		MessageDTO messageDTO = new MessageDTO();
		messageDTO.setToUser(esReceiver);
		messageDTO.setTitle(esTitle);
		messageDTO.setContent(esContent);
		messageDTO.setToAll(false);
		sendMessage(messageDTO);
	}

	@Override
	public void sendMessage(MessageDTO messageDTO) {
		wechatEnterpriseService.sendMessage(messageDTO, true);
	}

}
