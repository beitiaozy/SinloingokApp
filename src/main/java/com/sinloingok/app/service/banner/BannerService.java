package com.sinloingok.app.service.banner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.sinloingok.app.dao.status.NetSiteStatus;
import com.sinloingok.app.dtos.common.NetSiteInfoDto;
import com.sinloingok.app.service.activity.ActivityIntroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinloingok.app.dao.BannerDao;
import com.sinloingok.app.models.home.HomeImg;

/**
 * Service for accessing banner images.
 */
@Service
public class BannerService {

    @Autowired
    private BannerDao bannerDao;
    @Autowired
    private ActivityIntroService activityIntroService;

    /**
     * Retrieve banner images by activity type.
     *
     * @param activityType banner category
     * @return list of images
     */
    public List<HomeImg> listByType(String activityType) {
        return bannerDao.selectByActivityType(activityType);
    }

    public NetSiteInfoDto selectInfoByOnlyCode(String onlyCode){
        NetSiteInfoDto infoDto = bannerDao.selectInfoByOnlyCode(onlyCode, NetSiteStatus.DELETE);
        List<Map<String, BigDecimal>> freeMap = bannerDao.selectSlgFreeMapper();
        infoDto.setFreeMap(freeMap);
        /**
         * 	平台充值协议
         */
        String recharge_description = activityIntroService.findById(2L).getDescription();
        infoDto.setRechargeDescription(recharge_description);
        return infoDto;
    }
}
