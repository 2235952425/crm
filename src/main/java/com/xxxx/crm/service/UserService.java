package com.xxxx.crm.service;

import com.github.pagehelper.util.StringUtil;
import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.SaleChanceMapper;
import com.xxxx.crm.dao.UserMapper;
import com.xxxx.crm.dao.UserRoleMapper;
import com.xxxx.crm.model.UserModel;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.utils.Md5Util;
import com.xxxx.crm.utils.PhoneUtil;
import com.xxxx.crm.utils.UserIDBase64;
import com.xxxx.crm.vo.User;
import com.xxxx.crm.vo.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserService extends BaseService<User,Integer> {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    /**
     * 用户登录
     * 1.参数校验
     *    用户名  非空
     *    密码    非空
     * 2.根据用户名  查询用户记录
     * 3.用户存在性校验
     *     不存在   -->记录不存在  方法结束
     * 4.用户存在
     *     校验密码
     *        密码错误 -->密码不正确   方法结束
     * 5.密码正确
     *     用户登录成功  返回用户信息
     */
    public UserModel userLogin(String userName,String userPwd){

        checkLoginParams(userName,userPwd);

        User user = userMapper.queryUserByName(userName);

        AssertUtil.isTrue(user==null,"用户名不存在------service层");

        checkUserPwd(userPwd,user.getUserPwd());

        return buildUserInfo(user);

    }

    private UserModel buildUserInfo(User user) {
        UserModel userModel = new UserModel();
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        return userModel;
    }

    private void checkUserPwd(String userPwd, String Pwd) {

        AssertUtil.isTrue(!Md5Util.encode(userPwd).equals(Pwd),"用户密码不正确");

    }

    private void checkLoginParams(String userName, String userPwd) {

        AssertUtil.isTrue(StringUtils.isBlank(userName),"用户名不能为空------service层");

        AssertUtil.isTrue(StringUtils.isBlank(userPwd),"用户密码不能为空------service层");

    }

    //用户修改密码
    @Transactional(propagation = Propagation.REQUIRED)
    public void upDateUserPwd(String ordPwd,String newPwd,String repeatPwd,Integer userId){

        User user = userMapper.selectByPrimaryKey(userId);

        AssertUtil.isTrue(null==user,"用户id不能为空");

        checkPasswordParams(user,ordPwd,newPwd,repeatPwd);

        user.setUserPwd(Md5Util.encode(newPwd));

        AssertUtil.isTrue(userMapper.updateByPrimaryKey(user)<1,"修改密码失败！");
    }

    private void checkPasswordParams(User user,String ordPwd, String newPwd, String repeatPwd) {

        AssertUtil.isTrue(StringUtils.isBlank(ordPwd),"用户密码不能为空");
        AssertUtil.isTrue(!user.getUserPwd().equals(Md5Util.encode(ordPwd)),"用户密码不正确");

        AssertUtil.isTrue(StringUtils.isBlank(newPwd),"新密码不能为空");
        AssertUtil.isTrue(ordPwd.equals(newPwd),"新密码不能与就密码相同");
        AssertUtil.isTrue(StringUtils.isBlank(repeatPwd),"重复密码不能为空");

        AssertUtil.isTrue(!newPwd.equals(repeatPwd),"两次密码不一致");


    }

    public List<Map<String,Object>> queryAllSales(){
        return userMapper.queryAllCustomerManagers();
    }
    /**
     * 添加用户
     *  1. 参数校验
     *      用户名userName     非空，唯一性
     *      邮箱email          非空
     *      手机号phone        非空，格式正确
     *  2. 设置参数的默认值
     *      isValid           1
     *      createDate        系统当前时间
     *      updateDate        系统当前时间
     *      默认密码            123456 -> md5加密
     *  3. 执行添加操作，判断受影响的行数
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param user
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user){

        checkUserParams(user.getUserName(),user.getEmail(),user.getPhone(),null);

        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        user.setIsValid(1);

        user.setUserPwd(Md5Util.encode("123456"));

        AssertUtil.isTrue(userMapper.insertSelective(user)!=1,"添加失败");

        //添加用户角色绑定
        /* 用户角色关联 */
        /**
         * 用户ID
         *  userId
         * 角色ID
         *  roleIds
         */
        relationUserRole(user.getId(), user.getRoleIds());
    }
    /**
     * 用户角色关联
     *  添加操作
     *      原始角色不存在
     *          1. 不添加新的角色记录    不操作用户角色表
     *          2. 添加新的角色记录      给指定用户绑定相关的角色记录
     *
     *  更新操作
     *      原始角色不存在
     *          1. 不添加新的角色记录     不操作用户角色表
     *          2. 添加新的角色记录       给指定用户绑定相关的角色记录
     *      原始角色存在
     *          1. 添加新的角色记录       判断已有的角色记录不添加，添加没有的角色记录
     *          2. 清空所有的角色记录     删除用户绑定角色记录
     *          3. 移除部分角色记录       删除不存在的角色记录，存在的角色记录保留
     *          4. 移除部分角色，添加新的角色    删除不存在的角色记录，存在的角色记录保留，添加新的角色
     *
     *   如何进行角色分配？？？
     *      判断用户对应的角色记录存在，先将用户原有的角色记录删除，再添加新的角色记录
     *
     *  删除操作
     *      删除指定用户绑定的角色记录
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param userId  用户ID
     * @param roleIds 角色ID
     * @return
     */
    private void relationUserRole(Integer userId, String roleIds) {
        Integer count = userRoleMapper.countUserRoleByUserId(userId);
        if (count>0){
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"用户角色分配失败---删除");
        }
        if (StringUtils.isNotBlank(roleIds)){
            List<UserRole> userRoleList = new ArrayList<>();
            String[] roleIdsArray = roleIds.split(",");
            for (String roleId : roleIdsArray) {
                UserRole userRole = new UserRole();
                userRole.setRoleId(Integer.parseInt(roleId));
                userRole.setUserId(userId);
                userRole.setCreateDate(new Date());
                userRole.setUpdateDate(new Date());
                userRoleList.add(userRole);
            }
            AssertUtil.isTrue(userRoleMapper.insertBatch(userRoleList)!=userRoleList.size(),"用户角色分配失败");
        }
    }

    /**
     *  参数校验
     *        用户名userName     非空，唯一性
     *        邮箱email          非空
     *        手机号phone        非空，格式正确
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param userName
     * @param email
     * @param phone
     * @return void
     */
    private void checkUserParams(String userName, String email, String phone, Integer userId) {
        // 判断用户名是否为空
        AssertUtil.isTrue(StringUtils.isBlank(userName), "用户名不能为空！");
        // 判断用户名的唯一性
        // 通过用户名查询用户对象
        User temp = userMapper.queryUserByName(userName);
        // 如果用户对象为空，则表示用户名可用；如果用户对象不为空，则表示用户名不可用
        // 如果是添加操作，数据库中无数据，只要通过名称查到数据，则表示用户名被占用
        // 如果是修改操作，数据库中有对应的记录，通过用户名查到数据，可能是当前记录本身，也可能是别的记录
        // 如果用户名存在，且与当前修改记录不是同一个，则表示其他记录占用了该用户名，不可用
        AssertUtil.isTrue(null != temp && !(temp.getId().equals(userId)), "用户名已存在，请重新输入！");

        // 邮箱 非空
        AssertUtil.isTrue(StringUtils.isBlank(email), "用户邮箱不能为空！");

        // 手机号 非空
        AssertUtil.isTrue(StringUtils.isBlank(phone), "用户手机号不能为空！");

        // 手机号 格式判断
        AssertUtil.isTrue(!PhoneUtil.isMobile(phone), "手机号格式不正确！");
    }

    /**
     * 更新用户
     *  1. 参数校验
     *      判断用户ID是否为空，且数据存在
     *      用户名userName     非空，唯一性
     *      邮箱email          非空
     *      手机号phone        非空，格式正确
     *  2. 设置参数的默认值
     *      updateDate        系统当前时间
     *  3. 执行更新操作，判断受影响的行数
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param user
     * @return void
     */
    public void updateUser(User user){
        AssertUtil.isTrue(null == user.getId(), "待更新记录不存在！");
        User temp = userMapper.selectByPrimaryKey(user.getId());
        AssertUtil.isTrue(null==temp,"待更新记录不存在！");

        checkUserParams(user.getUserName(),user.getEmail(),user.getPhone(),user.getId());

        user.setUpdateDate(new Date());

        //添加用户角色绑定
        /* 用户角色关联 */
        /**
         * 用户ID
         *  userId
         * 角色ID
         *  roleIds
         */
        relationUserRole(user.getId(), user.getRoleIds());

        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user)!=1,"用户信息更新失败");
    }

    /**
     * 用户删除
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param ids
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteByIds(Integer[] ids){
        AssertUtil.isTrue(ids==null||ids.length==0,"待删除记录不存在！");
        AssertUtil.isTrue(userMapper.deleteBatch(ids) != ids.length, "用户删除失败！");

        for (Integer userId : ids) {

            Integer count = userRoleMapper.countUserRoleByUserId(userId);

            if (count>0){
                AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"删除用户没删除用户角色没有成功");
            }

        }
    }
}
