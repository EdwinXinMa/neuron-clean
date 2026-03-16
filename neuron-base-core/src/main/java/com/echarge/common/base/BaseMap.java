package com.echarge.common.base;

import cn.hutool.core.util.ObjectUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.beanutils.ConvertUtils;

public class BaseMap extends HashMap<String, Object> {
    private static final long serialVersionUID = 1L;

    public BaseMap() {
    }

    public BaseMap(Map<String, Object> map) {
        this.putAll(map);
    }

    @Override
    public BaseMap put(String key, Object value) {
        super.put(key, Optional.ofNullable(value).orElse(""));
        return this;
    }

    public BaseMap add(String key, Object value) {
        super.put(key, Optional.ofNullable(value).orElse(""));
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Object obj = super.get(key);
        if (ObjectUtil.isNotEmpty(obj)) {
            return (T) obj;
        }
        return null;
    }

    public Boolean getBoolean(String key) {
        Object obj = super.get(key);
        if (ObjectUtil.isNotEmpty(obj)) {
            return Boolean.valueOf(obj.toString());
        }
        return false;
    }

    public Long getLong(String key) {
        Object v = super.get(key);
        if (ObjectUtil.isNotEmpty(v)) {
            return Long.valueOf(v.toString());
        }
        return null;
    }

    public Long[] getLongs(String key) {
        Object v = super.get(key);
        if (ObjectUtil.isNotEmpty(v)) {
            return (Long[]) v;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Long> getListLong(String key) {
        Object obj = super.get(key);
        if (ObjectUtil.isNotEmpty(obj)) {
            List<?> list = (List<?>) obj;
            return list.stream().map(e -> Long.valueOf(e.toString())).collect(Collectors.toList());
        }
        return null;
    }

    public Long[] getLongIds(String key) {
        Object ids = super.get(key);
        if (ObjectUtil.isNotEmpty(ids)) {
            return (Long[]) ConvertUtils.convert(ids.toString().split(","), Long.class);
        }
        return null;
    }

    public Integer getInt(String key, Integer def) {
        Object v = super.get(key);
        if (ObjectUtil.isNotEmpty(v)) {
            return Integer.parseInt(v.toString());
        }
        return def;
    }

    public Integer getInt(String key) {
        Object v = super.get(key);
        if (ObjectUtil.isNotEmpty(v)) {
            return Integer.parseInt(v.toString());
        }
        return 0;
    }

    public BigDecimal getBigDecimal(String key) {
        Object v = super.get(key);
        if (ObjectUtil.isNotEmpty(v)) {
            return new BigDecimal(v.toString());
        }
        return new BigDecimal("0");
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T def) {
        Object obj = super.get(key);
        if (ObjectUtil.isEmpty(obj)) {
            return def;
        }
        return (T) obj;
    }

    public static BaseMap toBaseMap(Map<String, Object> obj) {
        BaseMap map = new BaseMap();
        map.putAll(obj);
        return map;
    }
}
