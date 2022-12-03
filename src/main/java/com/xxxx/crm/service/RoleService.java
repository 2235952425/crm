package com.xxxx.crm.service;

import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.ModuleMapper;
import com.xxxx.crm.dao.PermissionMapper;
import com.xxxx.crm.dao.RoleMapper;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.Permission;
import com.xxxx.crm.vo.Role;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
@Service
public class RoleService extends BaseService<Role,Integer> {
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private PermissionMapper permissionMapper;
    @Resource
    private ModuleMapper moduleMapper;

    /**
     * 查询所有的角色列表
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param
     * @return java.util.List<java.util.Map<java.lang.String,java.lang.Object>>
     */
    public List<Map<String,Object>> queryAllRoles(Integer userId){
        return roleMapper.queryAllRoles(userId);
    }

    /**
     * 添加角色
     *  1. 参数校验
     *      角色名称        非空，名称唯一
     *  2. 设置参数的默认值
     *      是否有效
     *      创建时间
     *      修改时间
     *  3. 执行添加操作，判断受影响的行数
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param role
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addRoLe(Role role){

        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"角色名不能为空");

        Role temp = roleMapper.selectByRoleName(role.getRoleName());

        AssertUtil.isTrue(null!=temp,"角色名已存在");

        role.setCreateDate(new Date());
        role.setUpdateDate(new Date());
        role.setIsValid(1);

        AssertUtil.isTrue(roleMapper.insertSelective(role)<1,"角色创建失败");

    }
    /**
     * 修改角色
     *  1. 参数校验
     *      角色ID    非空，且数据存在
     *      角色名称   非空，名称唯一
     *  2. 设置参数的默认值
     *      修改时间
     *  3. 执行更新操作，判断受影响的行数
     *
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param role
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateRole(Role role){

        AssertUtil.isTrue(role.getId()==null,"待更新记录不能为空");
        Role temp = roleMapper.selectByPrimaryKey(role.getId());
        AssertUtil.isTrue(null==temp,"待更新记录不存在");

        AssertUtil.isTrue(role.getRoleName()==null,"角色名不能为空");
        temp = roleMapper.selectByRoleName(role.getRoleName());
        AssertUtil.isTrue(null!=temp&&temp.getRoleName()==role.getRoleName(),"角色名已经存在,请重新输入");

        role.setUpdateDate(new Date());

        AssertUtil.isTrue(roleMapper.updateByPrimaryKeySelective(role)<1,"更新角色失败");


    }

    /**
     * 删除角色
     *  1. 参数校验
     *      角色ID    非空，数据存在
     *  2. 设置相关参数的默认
     *      是否有效    0（删除记录）
     *      修改时间    系统默认时间
     *  3. 执行更新操作，判断受影响的行数
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param roleId
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteRole(Integer roleId){
        AssertUtil.isTrue(null==roleId,"待更新记录ID没有传进来");

        Role temp = roleMapper.selectByPrimaryKey(roleId);
        AssertUtil.isTrue(null==temp,"待更新记录不存在");

        temp.setUpdateDate(new Date());
        temp.setIsValid(0);

        AssertUtil.isTrue(roleMapper.updateByPrimaryKeySelective(temp)<1,"角色删除失败");
    }

    /**
     * 角色授权
     *
     *  将对应的角色ID与资源ID，添加到对应的权限表中
     *      直接添加权限：不合适，会出现重复的权限数据（执行修改权限操作后删除权限操作时）
     *      推荐使用：
     *          先将已有的权限记录删除，再将需要设置的权限记录添加
     *          1. 通过角色ID查询对应的权限记录
     *          2. 如果权限记录存在，则删除对应的角色拥有的权限记录
     *          3. 如果有权限记录，则添加权限记录 (批量添加)
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param roleId
     * @param mIds
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addGrant(Integer roleId,Integer[] mIds){

        Integer count = permissionMapper.countPermissionByRoleId(roleId);

        if (count>0){
            permissionMapper.deletePermissionByRoleId(roleId);
        }

        if (mIds != null&&mIds.length>0){
            List<Permission> permissionList = new ArrayList<>();
            for (Integer mId : mIds) {
                Permission permission = new Permission();
                permission.setCreateDate(new Date());
                permission.setUpdateDate(new Date());
                permission.setModuleId(mId);
                permission.setRoleId(roleId);
                permission.setAclValue(moduleMapper.selectByPrimaryKey(mId).getOptValue());
                permissionList.add(permission);
            }
            AssertUtil.isTrue(permissionMapper.insertBatch(permissionList)!=permissionList.size(),"角色授权失败");
        }

    }

}
