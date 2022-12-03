package com.xxxx.crm.controller;

import com.xxxx.crm.annoation.RequiredPermission;
import com.xxxx.crm.base.BaseController;
import com.xxxx.crm.base.ResultInfo;
import com.xxxx.crm.enums.StateStatus;
import com.xxxx.crm.query.SaleChanceQuery;
import com.xxxx.crm.service.SaleChanceService;
import com.xxxx.crm.utils.CookieUtil;
import com.xxxx.crm.utils.LoginUserUtil;
import com.xxxx.crm.vo.SaleChance;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping("sale_chance")
public class SaleChanceController extends BaseController {
    @Resource
    private SaleChanceService saleChanceService;
    //查询
    @RequiredPermission(code = "101001")
    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> querySaleChanceByParams(SaleChanceQuery saleChanceQuery,Integer flag,HttpServletRequest request){
        //判断flag =1查询客户开发计划
        if(flag != null && flag==1){
            //设置分配状态
            saleChanceQuery.setState(StateStatus.STATED.getType());
            //从cookie中获取当前登录用户Id
            Integer userId = LoginUserUtil.releaseUserIdFromCookie(request);
            //设置查询的指派人为当前登录用户
            saleChanceQuery.setAssignMan(userId);
        }
        return saleChanceService.querySaleChanceByParams(saleChanceQuery);
    }

    @RequiredPermission(code = "1010")
    @RequestMapping("index")
    public String index(){
        return "saleChance/sale_chance";
    }

    /**
     * 添加营销机会 101002
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param saleChance
     * @return com.xxxx.crm.base.ResultInfo
     */
    @RequiredPermission(code = "101002")
    @RequestMapping("add")
    @ResponseBody
    public ResultInfo addSaleChance(SaleChance saleChance, HttpServletRequest request) {

        String userName = CookieUtil.getCookieValue(request,"userName");

        saleChance.setCreateMan(userName);

        saleChanceService.addSaleChance(saleChance);

        return success("营销机会数据添加成功！");
    }

    /**
     * 进入添加/修改营销机会数据页面
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param
     * @return java.lang.String
     */
    @RequestMapping("toSaleChancePage")
    public String toSaleChancePage(Integer saleChanceId, HttpServletRequest request) {
        // 判断saleChanceId是否为空
        if (saleChanceId != null) {
            // 通过ID查询营销机会数据
            SaleChance saleChance = saleChanceService.selectByPrimaryKey(saleChanceId);
            // 将数据设置到请求域中
            request.setAttribute("saleChance",saleChance);
        }
        return "saleChance/add_update";
    }

    /**
 * 更新营销机会 101004
 *
 *
 * 乐字节：专注线上IT培训
 * 答疑老师微信：lezijie
 * @param saleChance
 * @return com.xxxx.crm.base.ResultInfo
 */
    @RequiredPermission(code = "101004")
    @PostMapping ("update")
    @ResponseBody
    public ResultInfo updateSaleChance(SaleChance saleChance){

        saleChanceService.updateSaleChance(saleChance);

        return success("营销机会数据更新成功！");

    }

    /**
     * 删除营销机会 101003
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param ids
     * @return com.xxxx.crm.base.ResultInfo
     */
    @RequiredPermission(code = "101003")
    @PostMapping("delete")
    @ResponseBody
    public ResultInfo deleteSaleChance(Integer[] ids){
        saleChanceService.deleteSaleChance(ids);
        return success("营销机会数据删除成功！");
    }

    /**
     * 更新营销机会的开发状态
     *
     *
     * 乐字节：专注线上IT培训
     * 答疑老师微信：lezijie
     * @param id
     * @param devResult
     * @return com.xxxx.crm.base.ResultInfo
     */
    @PostMapping("updateSaleChanceDevResult")
    @ResponseBody
    public ResultInfo updateSaleChanceDevResult(Integer id, Integer devResult) {

        saleChanceService.updateSaleChanceDevResult(id, devResult);

        return success("开发状态更新成功！");

    }
}
