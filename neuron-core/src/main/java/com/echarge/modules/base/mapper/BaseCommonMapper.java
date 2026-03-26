package com.echarge.modules.base.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Param;
import com.echarge.common.api.dto.LogDTO;

/**
 * @Description: BaseCommonMapper
 * @author Edwin
 */
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BaseCommonMapper {

    /**
     * 保存日志
     * @param dto 日志数据传输对象
     */
    @InterceptorIgnore(illegalSql = "true", tenantLine = "true")
    void saveLog(@Param("dto")LogDTO dto);

}
