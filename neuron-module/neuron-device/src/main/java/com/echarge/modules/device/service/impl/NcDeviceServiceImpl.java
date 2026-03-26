package com.echarge.modules.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.echarge.common.constant.BizConstant;
import com.echarge.common.exception.NeuronBootException;
import com.echarge.modules.device.entity.NcDevice;
import com.echarge.modules.device.mapper.NcDeviceMapper;
import com.echarge.modules.device.service.INcDeviceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Edwin
 */
@Service
public class NcDeviceServiceImpl extends ServiceImpl<NcDeviceMapper, NcDevice> implements INcDeviceService {

    /** {@inheritDoc} */
    @Override
    public void register(NcDevice device) {
        String sn = device.getSn();
        if (StringUtils.isNotBlank(sn)) {
            sn = sn.trim().replaceAll("\\s+", "").toUpperCase();
            device.setSn(sn);
        }
        if (StringUtils.isBlank(device.getSn())) {
            throw new NeuronBootException("\u8bbe\u5907SN\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (StringUtils.isBlank(device.getDealer())) {
            throw new NeuronBootException("\u7ecf\u9500\u5546\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (device.getShipDate() == null) {
            throw new NeuronBootException("\u51fa\u8d27\u65e5\u671f\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (existsBySn(device.getSn())) {
            throw new NeuronBootException("\u8bbe\u5907SN\u5df2\u5b58\u5728");
        }
        device.setDeviceType(BizConstant.TYPE_N3_LITE);
        device.setOnlineStatus(BizConstant.DEVICE_UNACTIVATED);
        device.setStatus("NORMAL");
        device.setDelFlag(0);
        assignRandomLocation(device);
        this.save(device);
    }

    /** {@inheritDoc} */
    @Override
    public void disable(String id) {
        NcDevice device = this.getById(id);
        if (device == null) {
            throw new NeuronBootException("\u8bbe\u5907\u4e0d\u5b58\u5728");
        }
        device.setStatus("DISABLED");
        this.updateById(device);
    }

    /** {@inheritDoc} */
    @Override
    public void enable(String id) {
        NcDevice device = this.getById(id);
        if (device == null) {
            throw new NeuronBootException("\u8bbe\u5907\u4e0d\u5b58\u5728");
        }
        device.setStatus("NORMAL");
        this.updateById(device);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsBySn(String sn) {
        return this.count(new LambdaQueryWrapper<NcDevice>()
                .eq(NcDevice::getSn, sn)
                .eq(NcDevice::getDelFlag, 0)) > 0;
    }

    /**
     * 如果设备没有经纬度，随机分配一个全球陆地坐标
     */
    public static void assignRandomLocation(NcDevice device) {
        if (device.getLat() != null && device.getLng() != null) {
            return;
        }
        // 全球主要城市坐标 { 纬度, 经度 } + 小范围偏移，保证落在陆地上
        double[][] cities = {
            // 北京
            { 39.9, 116.4 },
            // 上海
            { 31.2, 121.5 },
            // 广州
            { 23.1, 113.3 },
            // 深圳
            { 22.5, 114.1 },
            // 东京
            { 35.7, 139.7 },
            // 首尔
            { 37.6, 127.0 },
            // 新加坡
            { 1.35, 103.8 },
            // 曼谷
            { 13.8, 100.5 },
            // 新德里
            { 28.6, 77.2 },
            // 伦敦
            { 51.5, -0.1 },
            // 巴黎
            { 48.9, 2.35 },
            // 柏林
            { 52.5, 13.4 },
            // 马德里
            { 40.4, -3.7 },
            // 罗马
            { 41.9, 12.5 },
            // 纽约
            { 40.7, -74.0 },
            // 洛杉矶
            { 34.1, -118.2 },
            // 多伦多
            { 43.7, -79.4 },
            // 圣保罗
            { -23.6, -46.6 },
            // 开普敦
            { -33.9, 18.4 },
            // 悉尼
            { -33.9, 151.2 },
            // 墨尔本
            { -37.8, 145.0 },
            // 莫斯科
            { 55.8, 37.6 },
            // 迪拜
            { 25.3, 55.3 },
        };
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[] city = cities[rng.nextInt(cities.length)];
        // ±1度 ≈ ±111km
        double lat = city[0] + (rng.nextDouble() - 0.5) * 2;
        double lng = city[1] + (rng.nextDouble() - 0.5) * 2;
        device.setLat(BigDecimal.valueOf(lat).setScale(6, RoundingMode.HALF_UP));
        device.setLng(BigDecimal.valueOf(lng).setScale(6, RoundingMode.HALF_UP));
    }
}