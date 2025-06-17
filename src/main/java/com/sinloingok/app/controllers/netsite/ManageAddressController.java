package com.sinloingok.app.controllers.netsite;

import com.sinloingok.app.aop.AuthCheck;
import com.sinloingok.app.aop.LoginType;
import com.sinloingok.app.aop.UserContext;
import com.sinloingok.app.constant.SinloingokUserStatus;
import com.sinloingok.app.constant.SysConstant;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.dao.status.BluetoothAddressStatus;
import com.sinloingok.app.dtos.StandardRtnDto;
import com.sinloingok.app.dtos.address.AddressResDto;
import com.sinloingok.app.models.bluetooth.BluetoothAddress;
import com.sinloingok.app.models.user.ManageUser;
import com.sinloingok.app.service.netsite.BluetoothAddressService;
import com.sinloingok.app.util.DateUtils;
import com.sinloingok.app.util.PropertiesUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 场地管理
 *
 * @author Jun
 */
@RestController
@RequestMapping("manageAddress")
public class ManageAddressController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(ManageAddressController.class);
    private final static String mch_id = PropertiesUtils.getProperties().getProperty("WEIXIN_MCHID");
    private final static String private_key = PropertiesUtils.getProperties().getProperty("WEIXIN_PRIVATE_KEY");

    @Autowired
    private BluetoothAddressService bluetoothAddressService;

    /**
     * 员工查看场所列表
     */
    @RequestMapping("addressList")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<?> addressList() {
        ManageUser user = UserContext.getMUser();
        return success(bluetoothAddressService.listByUserId(user.getId()));
    }

    /**
     * 员工添加场所
     */
    @PostMapping("addAddress")
    @AuthCheck(LoginType.MANAGER)
    public StandardRtnDto<String> addAddress(@RequestBody AddressResDto dto) {
        ManageUser user = UserContext.getMUser();
        String addr_name = dto.getAddrName();
        String addr_person_name = dto.getAddrPersonName();
        String addr_person_mobile = dto.getAddrPersonMobile();
        String img = dto.getImg();
        String imges = dto.getImges();

        String shou_ye = dto.getShouYe();
        if (ObjectUtils.anyNull(shou_ye)) {
            shou_ye = "";
        }
        String huo_dong = dto.getHuoDong();
        if (ObjectUtils.anyNull(huo_dong)) {
            huo_dong = "";
        }
        String chong_zhi = dto.getChongZhi();
        if (ObjectUtils.anyNull(chong_zhi)) {
            chong_zhi = "";
        }

//		Float opendoor_recharge_min = getFloat("opendoor_recharge_min");// 开柜最低单次充值金额
//		Float opendoor_over_min = getFloat("opendoor_over_min");// 开柜最低剩余余额


        try {
            if (!(user.getRoleId() == SinloingokUserStatus.RoleID.DAILI || user.getRoleId() == SinloingokUserStatus.RoleID.SHANGHU)) {
                return new StandardRtnDto<>(SysConstant.ResultCode.ERROR, "权限不足");
            }

            BluetoothAddress address = bluetoothAddressService.findByUserIdAndName(user.getId(), addr_name);

            if (address != null) {
                return new StandardRtnDto<>(SysConstant.ResultCode.ERROR, "场所名称禁止重复");
            }

            address = new BluetoothAddress();
            address.setUserId(user.getId());
            address.setAddrName(addr_name);
            address.setAddrPersonName(addr_person_name);
            address.setAddrPersonMobile(addr_person_mobile);
            address.setImg(img);
            address.setImges(imges);
            address.setStatus(BluetoothAddressStatus.NORMAL);
            address.setCreateTime(DateUtils.curTime());
            address.setWxMchid(mch_id);
            address.setWxapiv2PrivateKey(private_key);
            address.setShouYe(shou_ye);
            address.setHuoDong(huo_dong);
            address.setChongZhi(chong_zhi);
//			address.set("opendoor_recharge_min", opendoor_recharge_min);
//			address.set("opendoor_over_min", opendoor_over_min);
            bluetoothAddressService.insert(address);

            return new StandardRtnDto<>(SysConstant.ResultCode.SUCCESS, "添加场所成功");
        } catch (Exception e) {
            logger.error("添加场所系统错误", e);
//            DbKit.getThreadLocalConnection().rollback();
            return new StandardRtnDto<>(SysConstant.ResultCode.ERROR, "添加场所系统错误");
        }
    }

    /**
     * 员工删除场所
     */
    @RequestMapping("delAddress")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> delAddress(@RequestBody Map<String, String> params) {
        long address_id = Long.valueOf(params.getOrDefault("address_id", "-1"));
        ManageUser user = UserContext.getMUser();
        try {
            if (!(user.getRoleId() == SinloingokUserStatus.RoleID.DAILI || user.getRoleId() == SinloingokUserStatus.RoleID.SHANGHU)) {
                return error("权限不足");
            }

            BluetoothAddress address = bluetoothAddressService.findById(address_id);

            if (address == null) {
                return error("请提供正确的ID");
            }

            address.setStatus(BluetoothAddressStatus.DELETE);
            address.setUpdateTime(DateUtils.curTime());
            bluetoothAddressService.updateStatus(address_id, BluetoothAddressStatus.DELETE, DateUtils.curTime());

            return success("删除场所成功");
        } catch (Exception e) {
            logger.error("删除场所系统错误", e);
//            DbKit.getThreadLocalConnection().rollback();
            return error("删除场所系统错误");
        }
    }

    /**
     * 员工还原场所
     *
     * @throws Exception
     */
    @RequestMapping("reductionAddress")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> reductionAddress(@RequestBody Map<String, String> params) {
        long address_id = Long.valueOf(params.getOrDefault("address_id", "-1"));
        ManageUser user = UserContext.getMUser();
        try {
            BluetoothAddress address = bluetoothAddressService.findById(address_id);
            if (address == null) {
                return error("请提供正确的ID");
            }
            address.setStatus(BluetoothAddressStatus.NORMAL);
            address.setUpdateTime(DateUtils.curTime());
            bluetoothAddressService.updateStatus(address_id, BluetoothAddressStatus.NORMAL, DateUtils.curTime());

            return success("还原场所成功");
        } catch (Exception e) {
            logger.error("还原场所系统错误", e);
//            DbKit.getThreadLocalConnection().rollback();
        }
        return error("还原场所系统错误");
    }

    /**
     * 员工修改场所
     */
    @RequestMapping("editAddress")
    @AuthCheck(LoginType.MANAGER)
    @ResponseBody
    public StandardRtnDto<?> editAddress(@RequestBody BluetoothAddress address) {
        ManageUser user = UserContext.getMUser();
        try {
            if (!(user.getRoleId() == SinloingokUserStatus.RoleID.DAILI || user.getRoleId() == SinloingokUserStatus.RoleID.SHANGHU)) {
                return error("权限不足");
            }

            bluetoothAddressService.update(address);

            return success("修改场所成功");
        } catch (Exception e) {
            logger.error("修改场所系统错误", e);
//            DbKit.getThreadLocalConnection().rollback();
        }
        return error("修改场所系统错误");
    }
}
