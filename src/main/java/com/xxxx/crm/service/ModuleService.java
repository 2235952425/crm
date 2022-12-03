package com.xxxx.crm.service;

import com.xxxx.crm.base.BaseService;
import com.xxxx.crm.dao.ModuleMapper;
import com.xxxx.crm.dao.PermissionMapper;
import com.xxxx.crm.model.TreeModel;
import com.xxxx.crm.utils.AssertUtil;
import com.xxxx.crm.vo.Module;
import com.xxxx.crm.vo.Permission;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class ModuleService extends BaseService<Module,Integer> {
    @Resource
    private ModuleMapper moduleMapper;
    @Resource
    private PermissionMapper permissionMapper;
    /**
     * 查询所有的资源列表
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param
     * @return java.util.List<com.xxxx.crm.model.TreeModel>
     */
    public List<TreeModel> queryAllModules(Integer roleId){
        List<TreeModel> treeModelList = moduleMapper.queryAllModules();
        // 查询指定角色已经授权过的资源列表 (查询角色拥有的资源ID)
        List<Integer> permissionIds = permissionMapper.queryRoleHasModuleIdsByRoleId(roleId);
        // 判断角色是否拥有资源ID
        if (permissionIds != null && permissionIds.size() > 0) {
            // 循环所有的资源列表，判断用户拥有的资源ID中是否有匹配的，如果有，则设置checked属性为true
            treeModelList.forEach(treeModel -> {
                // 判断角色拥有的资源ID中是否有当前遍历的资源ID
                if (permissionIds.contains(treeModel.getId())) {
                    // 如果包含你，则说明角色授权过，设置checked为true
                    treeModel.setChecked(true);
                }
            });
        }
        return treeModelList;
    }
    /** 查询资源数据
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param
     * @return java.util.Map<java.lang.String,java.lang.Object>
     */
    public Map<String,Object>  queryModuleList(){
        Map<String,Object> map = new HashMap<>();
        List<Module> moduleList = new ArrayList<>();
        moduleList = moduleMapper.queryModuleList();
        map.put("code",0);
        map.put("msg","");
        map.put("count", moduleList.size());
        map.put("data",moduleList);
        return map;
    }

    /**
     * 添加资源
     *  1. 参数校验
     *      模块名称 moduleName
     *          非空，同一层级下模块名称唯一
     *      地址 url
     *          二级菜单（grade=1），非空且同一层级下不可重复
     *      父级菜单 parentId
     *          一级菜单（目录 grade=0）    -1
     *          二级|三级菜单（菜单|按钮 grade=1或2）    非空，父级菜单必须存在
     *      层级 grade
     *          非空，0|1|2
     *      权限码 optValue
     *          非空，不可重复
     *  2. 设置参数的默认值
     *      是否有效 isValid    1
     *      创建时间createDate  系统当前时间
     *      修改时间updateDate  系统当前时间
     *  3. 执行添加操作，判断受影响的行数
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param module
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addModule(Module module){
        Integer grade = module.getGrade();
        AssertUtil.isTrue(null==module.getModuleName(),"资源名不能为空");
        AssertUtil.isTrue(null!=moduleMapper.queryModuleByGradeAndModuleName(grade,module.getModuleName()),"改层级下模块名称已存在！");

        AssertUtil.isTrue(null==grade||!(grade==1||grade==2||grade==0),"菜单层级不合法！");

        if (grade==1){
            AssertUtil.isTrue(StringUtils.isBlank(module.getUrl()),"URL不能为空！");
            AssertUtil.isTrue(null!=moduleMapper.queryModuleByGradeAndUrl(grade,module.getUrl()),"URL不可重复！");
        }

        if (grade==0){
            module.setParentId(-1);
        }
        if (grade!=0){
            AssertUtil.isTrue(null == module.getParentId(),"父级菜单不能为空！");
            AssertUtil.isTrue(null==moduleMapper.selectByPrimaryKey(module.getParentId()),"请指定正确的父级菜单！");
        }

        AssertUtil.isTrue(null==module.getOptValue(),"权限码不能为空");
        AssertUtil.isTrue(null!=moduleMapper.queryModuleByOptValue(module.getOptValue()),"权限码已存在");

        module.setCreateDate(new Date());
        module.setUpdateDate(new Date());
        module.setIsValid((byte) 1);

        AssertUtil.isTrue(moduleMapper.insertSelective(module)<1,"添加资源失败！");

    }
    /**
         * 修改资源
         *  1. 参数校验
         *      id
         *          非空，数据存在
         *      层级 grade
         *          非空 0|1|2
         *      模块名称 moduleName
         *          非空，同一层级下模块名称唯一 （不包含当前修改记录本身）
         *      地址 url
         *          二级菜单（grade=1），非空且同一层级下不可重复（不包含当前修改记录本身）
         *      权限码 optValue
         *          非空，不可重复（不包含当前修改记录本身）
         *  2. 设置参数的默认值
         *      修改时间updateDate  系统当前时间
         *  3. 执行更新操作，判断受影响的行数
         *
         *
         * 乐字节：专注线上IT培训
         * 答疑老师微信：lezijie
         * @param module
         * @return void
         */
    @Transactional(propagation = Propagation.REQUIRED)
    public void  updateModule(Module module){

        AssertUtil.isTrue(null==module.getId(),"传入的修改的资源id有误");
        Module temp = moduleMapper.selectByPrimaryKey(module.getId());
        AssertUtil.isTrue(null==temp,"待更新记录不存在！");


        Integer grade = module.getGrade();
        AssertUtil.isTrue(null==grade||!(grade==1||grade==2||grade==0),"菜单层级不合法！");


        AssertUtil.isTrue(null==module.getModuleName(),"资源名不能为空");
        temp = moduleMapper.queryModuleByGradeAndModuleName(grade,module.getModuleName());
        if (null != temp ){
            AssertUtil.isTrue(!temp.getId().equals(module.getId()),"该层级下菜单名已存在！");
        }


        AssertUtil.isTrue(null==module.getOptValue(),"权限码不能为空");
        // 通过权限码查询资源对象
        temp = moduleMapper.queryModuleByOptValue(module.getOptValue());
        if (temp!=null){
            AssertUtil.isTrue(!(temp.getId()).equals(module.getId()),"权限码已存在");
        }


        if (grade==1){
            AssertUtil.isTrue(StringUtils.isBlank(module.getUrl()),"URL不能为空！");
            // 通过层级与菜单URl查询资源对象
            temp = moduleMapper.queryModuleByGradeAndUrl(grade, module.getUrl());
            // 判断是否存在
            if (temp != null) {
                AssertUtil.isTrue(!(temp.getId()).equals(module.getId()), "该层级下菜单URL已存在！");
            }
        }

        module.setUpdateDate(new Date());

        AssertUtil.isTrue(moduleMapper.updateByPrimaryKeySelective(module)<1,"修改资源失败");

    }

    /**
     * 删除资源
     *  1. 判断删除的记录是否存在
     *  2. 如果当前资源存在子记录，则不可删除
     *  3. 删除资源时，将对应的权限表的记录也删除（判断权限表中是否存在关联数据，如果存在，则删除）
     *  4. 执行删除（更新）操作，判断受影响的行数
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param id
     * @return void
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteModule(Integer id){

        AssertUtil.isTrue(null==id,"未指定删除资源的id");
        Module temp = moduleMapper.selectByPrimaryKey(id);
        AssertUtil.isTrue(null==temp,"待删除记录不存在");

        Integer count = moduleMapper.queryModuleByParentId(id);
        AssertUtil.isTrue(count>0,"该资源存在子记录，不可删除！");

        count = permissionMapper.countPermissionByModuleId(id);
        if (count>0){
            permissionMapper.deletePermissionByModuleId(id);
        }

        temp.setUpdateDate(new Date());
        temp.setIsValid((byte) 0);

        AssertUtil.isTrue(moduleMapper.updateByPrimaryKeySelective(temp)<1,"删除资源失败");


    }
}
