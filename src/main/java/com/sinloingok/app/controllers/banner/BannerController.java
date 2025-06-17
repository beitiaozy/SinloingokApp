package com.sinloingok.app.controllers.banner;

import com.sinloingok.app.dtos.common.NetSiteInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import com.sinloingok.app.controllers.base.BaseController;
import com.sinloingok.app.models.home.HomeImg;
import com.sinloingok.app.service.banner.BannerService;
import com.sinloingok.app.dtos.StandardRtnDto;

/**
 * 首页图片信息相关接口
 *
 * @author kaptenkabu
 * 所属权归 Sinloingok
 */
@Slf4j
@RestController
@RequestMapping("banner")
public class BannerController extends BaseController {

    /**
     * 返回所有图片
     */
    @Autowired
    private BannerService bannerService;

    @RequestMapping("imgList")
    public StandardRtnDto<?> imgList() {
        List<HomeImg> banner = bannerService.listByType("首页轮播");
        List<HomeImg> dan = bannerService.listByType("单图");
        List<HomeImg> shuang = bannerService.listByType("双图");

        Map<String, Object> record = new HashMap<>();
        record.put("banner", banner);
        record.put("dan", dan);
        record.put("shuang", shuang);

        return success(record);
    }

    /**
     * 扫码获取设备信息
     */
    @RequestMapping("getInfo")
    @ResponseBody
    public StandardRtnDto<?> getInfo(@RequestBody Map<String, String> params) {
        // 根據場地和用戶確定場地設備
        String dev_id = params.getOrDefault("dev_id", "");
        try {
            NetSiteInfoDto result = bannerService.selectInfoByOnlyCode(dev_id);
            return success(result);
        } catch (Exception e) {
            log.error("获取自助洗车机信息错误", e);
            return error("获取自助洗车机信息错误");
        }
    }

}
