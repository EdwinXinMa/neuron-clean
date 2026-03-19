package com.echarge.common.util.dynamic.db;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

import java.util.List;

/**
 * Freemarker 自定义方法：SQL 参数格式化
 * 替代原 org.jeecgframework.codegenerate 中的 SimpleFormat
 */
public class SimpleFormat implements TemplateMethodModelEx {
    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments == null || arguments.isEmpty()) {
            return "";
        }
        return arguments.get(0).toString();
    }
}
