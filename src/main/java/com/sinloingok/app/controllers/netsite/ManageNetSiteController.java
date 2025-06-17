package com.sinloingok.app.controllers.netsite;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.constant.SinloingokUserStatus;
import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dao.status.NetSiteStatus;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.models.user.ManageUser;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 设备相关
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@RestController
@RequestMapping("manageNetSite")
public class ManageNetSiteController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ManageNetSiteController.class);

    @Autowired
    private NetSiteService netSiteService;

    /**
     * 设备列表  -- TODO 設備查詢需要關聯用戶和設備地址
     */
    @RequestMapping("vmDeviceList")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> vmDeviceList(@RequestBody PageData<NetSite> pageData) {
        try {
            SimplePageDto<NetSite> result = netSiteService.vmDeviceList(pageData.getBody(), pageData);
            return success(result);
        } catch (Exception e) {
            logger.error("设备列表系统错误", e);
            return error("设备列表系统错误");
        }
    }

    /**
     * 录入设备
     *
     */
    @RequestMapping("addVMDevice")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> addVMDevice(@RequestBody NetSite netSite) {
        try {
            netSite.setUpdateTime(DateUtils.curTime());
            netSiteService.insertOrUpdateNetSite(netSite);
            return success("录入设备成功");
        } catch (Exception e) {
            logger.error("录入设备系统错误", e);
//            DbKit.getThreadLocalConnection().rollback();
            return error("录入设备系统错误");
        }
    }

    /**
     * 修改设备信息
     *
     * @throws Exception
     */
    @RequestMapping("editVMDevice")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> editVMDevice(@RequestBody NetSite netSite) throws Exception {
        ManageUser user = UserContext.getMUser();

        if (user.getStatus().equals(SinloingokUserStatus.ManageUserStatus.NO_USING)) {
            return error("账号异常！");
        }


        try {
            if (!(user.getRoleId() == SinloingokUserStatus.RoleID.DAILI || user.getRoleId() == SinloingokUserStatus.RoleID.SHANGHU)) {
                return error("权限不足");
            }

            netSite.setUpdateTime(DateUtils.curTime());
            netSiteService.insertOrUpdateNetSite(netSite);

            return success("修改设备信息成功");
        } catch (Exception e) {
            logger.error("修改设备信息系统错误", e);
//                        DbKit.getThreadLocalConnection().rollback();
            return error("修改设备信息系统错误");
        }
    }

    /**
     * 通过设备号获取其他信息
     */
    @RequestMapping("getVMDevice")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> getVMDevice(@RequestBody Map<String, String> params) {
        ManageUser user = UserContext.getMUser();

        if (user.getStatus().equals(SinloingokUserStatus.ManageUserStatus.NO_USING)) {
            return error("账号异常！");
        }

        String dev_id = params.getOrDefault("dev_id", "");// 扫码获取的售货机设备ID

        if (!(user.getRoleId() == SinloingokUserStatus.RoleID.DAILI || user.getRoleId() == SinloingokUserStatus.RoleID.SHANGHU)) {
            return error("权限不足");
        }

        NetSite netSite = netSiteService.selectByOnlyCode(dev_id);
        return success(netSite);
    }

    /**
     * 撤机
     */
    @RequestMapping("delVMDevice")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> delVMDevice(@RequestBody Map<String, String> params) {
        ManageUser user = UserContext.getMUser();
        long vmdevice_id = Long.valueOf(params.getOrDefault("vmdevice_id", "-1"));// 设备ID
        try {
            if (!(user.getRoleId() == SinloingokUserStatus.RoleID.DAILI || user.getRoleId() == SinloingokUserStatus.RoleID.SHANGHU)) {
                return error("权限不足");
            }
            NetSite netSite = netSiteService.selectById(vmdevice_id);
            if (netSite == null) {
                return error("设备信息错误");
            }

            netSite.setStatus(NetSiteStatus.DELETE);
            netSite.setUpdateTime(DateUtils.curTime());
            netSiteService.insertOrUpdateNetSite(netSite);
            return success("撤机成功");
        } catch (Exception e) {
            logger.error("撤机系统错误", e);
//                        DbKit.getThreadLocalConnection().rollback();
            return error("撤机系统错误");
        }
    }
}