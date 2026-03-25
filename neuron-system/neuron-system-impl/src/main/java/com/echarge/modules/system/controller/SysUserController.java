package com.echarge.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.echarge.common.api.vo.Result;
import com.echarge.common.constant.CommonConstant;
import com.echarge.common.system.vo.LoginUser;
import com.echarge.common.util.PasswordUtil;
import com.echarge.common.util.oConvertUtils;
import com.echarge.modules.system.entity.SysUser;
import com.echarge.modules.system.service.ISysUserService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 用户管理（精简版）
 * 功能：账号列表、新增账号、禁用/启用、重置密码、修改自己密码
 */
@Slf4j
@RestController
@RequestMapping("/sys/user")
public class SysUserController {

    @Autowired
    private ISysUserService sysUserService;

    /**
     * 用户列表（分页 + 搜索）
     */
    @GetMapping("/list")
    public Result<IPage<SysUser>> list(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "realname", required = false) String realname,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

        LambdaQueryWrapper<SysUser> query = new LambdaQueryWrapper<>();
        query.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0);
        if (StringUtils.isNotBlank(username)) {
            query.like(SysUser::getUsername, username);
        }
        if (StringUtils.isNotBlank(realname)) {
            query.like(SysUser::getRealname, realname);
        }
        if (status != null) {
            query.eq(SysUser::getStatus, status);
        }
        query.last("ORDER BY CASE WHEN username='admin' THEN 0 ELSE 1 END ASC, create_time DESC");

        IPage<SysUser> page = sysUserService.page(new Page<>(pageNo, pageSize), query);
        // 清除密码和盐值
        page.getRecords().forEach(u -> { u.setPassword(null); u.setSalt(null); });
        return Result.OK(page);
    }

    /**
     * 新增用户
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody SysUser user) {
        // 检查用户名是否重复
        SysUser existing = sysUserService.getUserByName(user.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }
        // 设置默认值
        String salt = oConvertUtils.randomGen(8);
        String password = user.getPassword();
        if (StringUtils.isBlank(password)) {
            password = "123456"; // 默认密码
        }
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(user.getUsername(), password, salt));
        user.setStatus(CommonConstant.USER_UNFREEZE); // 正常状态
        user.setDelFlag(CommonConstant.DEL_FLAG_0);
        user.setCreateTime(new Date());
        if (StringUtils.isBlank(user.getRole())) {
            user.setRole("operator"); // 默认运维角色
        }
        sysUserService.save(user);
        return Result.OK("添加成功");
    }

    /**
     * 修改用户基本信息（真实姓名、手机号、邮箱）
     */
    @PutMapping("/edit")
    public Result<String> edit(@RequestBody SysUser user) {
        if (user.getId() == null) {
            return Result.error("缺少用户ID");
        }
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setRealname(user.getRealname());
        update.setPhone(user.getPhone());
        update.setEmail(user.getEmail());
        sysUserService.updateById(update);
        return Result.OK("修改成功");
    }

    /**
     * 禁用/启用用户
     */
    @PutMapping("/freeze")
    public Result<String> freeze(@RequestParam("id") String id, @RequestParam("status") Integer status) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        sysUserService.updateById(user);
        return Result.OK(CommonConstant.USER_FREEZE.equals(status) ? "已禁用" : "已启用");
    }

    /**
     * 重置密码
     */
    @PutMapping("/resetPassword")
    public Result<String> resetPassword(@RequestParam("id") String id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        String salt = oConvertUtils.randomGen(8);
        String defaultPassword = "123456";
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(user.getUsername(), defaultPassword, salt));
        sysUserService.updateById(user);
        return Result.OK("密码已重置为默认密码");
    }

    /**
     * 修改自己的密码
     */
    @PutMapping("/changePassword")
    public Result<String> changePassword(@RequestBody com.alibaba.fastjson.JSONObject json) {
        String oldPassword = json.getString("oldpassword");
        String newPassword = json.getString("password");
        String confirmPassword = json.getString("confirmpassword");

        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.error("两次输入密码不一致");
        }

        SysUser user = sysUserService.getById(loginUser.getId());
        String encrypted = PasswordUtil.encrypt(user.getUsername(), oldPassword, user.getSalt());
        if (!encrypted.equals(user.getPassword())) {
            return Result.error("旧密码错误");
        }

        String salt = oConvertUtils.randomGen(8);
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(user.getUsername(), newPassword, salt));
        sysUserService.updateById(user);
        return Result.OK("密码修改成功");
    }

    /**
     * 更新当前用户头像
     */
    @PutMapping("/updateAvatar")
    public Result<String> updateAvatar(@RequestBody com.alibaba.fastjson.JSONObject json) {
        String avatar = json.getString("avatar");
        if (StringUtils.isBlank(avatar)) {
            return Result.error("头像地址不能为空");
        }
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        SysUser update = new SysUser();
        update.setId(loginUser.getId());
        update.setAvatar(avatar);
        sysUserService.updateById(update);
        return Result.OK(avatar);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/getUserInfo")
    public Result<SysUser> getUserInfo() {
        LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        if (loginUser == null) {
            return Result.error("请先登录");
        }
        SysUser user = sysUserService.getById(loginUser.getId());
        if (user != null) {
            user.setPassword(null);
            user.setSalt(null);
        }
        return Result.OK(user);
    }
}
