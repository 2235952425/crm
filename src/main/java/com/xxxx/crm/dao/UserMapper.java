package com.xxxx.crm.dao;

import com.xxxx.crm.base.BaseMapper;
import com.xxxx.crm.vo.User;

import java.util.List;
import java.util.Map;

public interface UserMapper extends BaseMapper<User,Integer> {
    Integer deleteByPrimaryKey(Integer id);

    int insert(User record);

    Integer insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    Integer updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    User queryUserByName(String userName);

    List<Map<String,Object>> queryAllCustomerManagers();
}