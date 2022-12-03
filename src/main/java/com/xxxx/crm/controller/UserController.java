package com.xxxx.crm.controller;

import com.xxxx.crm.base.BaseController;
import com.xxxx.crm.base.ResultInfo;
import com.xxxx.crm.exceptions.ParamsException;
import com.xxxx.crm.model.UserModel;
import com.xxxx.crm.query.UserQuery;
import com.xxxx.crm.service.SaleChanceService;
import com.xxxx.crm.service.UserService;
import com.xxxx.crm.utils.LoginUserUtil;
import com.xxxx.crm.vo.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RequestMapping("user")
@Controller
public class UserController extends BaseController{
    @Resource
    private UserService userService;
    /**
     * 用户登录
     *
     * @param userName
     * @param userPwd
     * @return
     */
    @PostMapping("login")
    @ResponseBody
    public ResultInfo userLogin(String userName,String userPwd){

        ResultInfo resultInfo = new ResultInfo();

        try {
            UserModel userModel = userService.userLogin(userName,userPwd);
            resultInfo.setResult(userModel);

        }catch (ParamsException p){
            resultInfo.setCode(p.getCode());
            resultInfo.setMsg(p.getMsg());
            p.printStackTrace();
        }catch (Exception e){
            resultInfo.setCode(500);
            resultInfo.setMsg("登录失败");
            e.printStackTrace();
        }

        return resultInfo;
    }

    //用户修改密码
    @PostMapping ("updatePassword")
    @ResponseBody
    //public ResultInfo userupDatePwd(String newPwd, String ordPwd, String repeatPwd, HttpServletRequest request){
    public ResultInfo userupDatePwd(String newPwd, String oldPwd, String repeatPwd, HttpServletRequest request){

        ResultInfo resultInfo = new ResultInfo();

        try {
            //查询session获得user
            Integer userId = LoginUserUtil.releaseUserIdFromCookie(request);

            userService.upDateUserPwd(oldPwd,newPwd,repeatPwd,userId);
            resultInfo.setMsg("用户密码修改成功");
            resultInfo.setCode(200);
        }catch (ParamsException p){
            resultInfo.setCode(p.getCode());
            resultInfo.setMsg(p.getMsg());
            p.printStackTrace();
        }catch (Exception e){
            resultInfo.setCode(500);
            resultInfo.setMsg("修改密码失败！");
            e.printStackTrace();
        }
        return resultInfo;
    }

    //用户修改密码页面
    @RequestMapping("toPasswordPage")
    public String userupDatePwdPage(){
        return "user/password";
    }

    @RequestMapping("queryAllSales")
    @ResponseBody
    private List<Map<String,Object>>queryAllSales(){
        return userService.queryAllSales();
    }

    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> selectByParams(UserQuery userQuery){
        return userService.queryByParamsForTable(userQuery);
    }

    @RequestMapping("index")
    public String index(){
        return "user/user";
    }

    @RequestMapping("add")
    @ResponseBody
    public ResultInfo addUser(User user){
        userService.addUser(user);
        return success("用户添加成功");
    }

    @RequestMapping("toAddOrUpdateUserPage")
    public String toAddOrUpdateUserPage(Integer id,HttpServletRequest request){
        if (null!=id){
            User user = userService.selectByPrimaryKey(id);
            request.setAttribute("userInfo",user);
        }
        return "user/add_update";
    }

    @RequestMapping("update")
    @ResponseBody
    public ResultInfo updateUser(User user){
        userService.updateUser(user);
        return success("用户添加成功");
    }
    /**
     * 用户删除
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param ids
     * @return com.xxxx.crm.base.ResultInfo
     */
    @RequestMapping("delete")
    @ResponseBody
    public ResultInfo deleteUser(Integer[] ids){
        userService.deleteByIds(ids);
        return success("用户删除成功");
    }
}
