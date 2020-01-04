package com.hopu.bigdata.service;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hopu.bigdata.mapper.UserinfoMapper;
import com.hopu.bigdata.model.Userinfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserinfoMapper, Userinfo> {

    @Autowired
    private UserinfoMapper userMapper;

    public Userinfo getUserByName(String name) {
        QueryWrapper<Userinfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("loginname", name);
        return userMapper.selectOne(queryWrapper);
    }

    public int updateUser(Userinfo user) {
        UpdateWrapper<Userinfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("loginname", user.getLoginname());
        return userMapper.update(user, updateWrapper);
    }
}
