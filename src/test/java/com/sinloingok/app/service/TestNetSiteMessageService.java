package com.sinloingok.app.service;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.TestSinloingokApplication;
import com.sinloingok.app.controllers.base.PageData;
import com.sinloingok.app.dao.status.OrderStatus;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.dtos.common.NetSiteInfoDto;
import com.sinloingok.app.dtos.order.NetSiteOrderResDto;
import com.sinloingok.app.dtos.order.NetSiteUserReqDto;
import com.sinloingok.app.dtos.user.RechargeRecordDto;
import com.sinloingok.app.models.net4g.NetSiteBillRule;
import com.sinloingok.app.models.net4g.NetSiteOrder;
import com.sinloingok.app.models.net4g.NetSiteOrderDetail;
import com.sinloingok.app.service.banner.BannerService;
import com.sinloingok.app.service.netsite.*;
import com.sinloingok.app.service.order.NetSiteOrderService;
import com.sinloingok.app.service.order.OrderSettlementService;
import com.sinloingok.app.service.user.UserMoneyRecordService;
import com.sinloingok.app.util.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

public class TestNetSiteMessageService extends TestSinloingokApplication {

    @Autowired
    private NetSiteOrderDetailService detailService;

    @Autowired
    private OrderSettlementService orderSettlementService;

    @Autowired
    private NetSiteService netSiteService;

    @Autowired
    private NetSiteOrderService orderService;
    @Autowired
    private UserMoneyRecordService userMoneyRecordService;
    @Autowired
    private BannerService bannerService;
    @Autowired
    private NetSiteBillRuleService ruleService;
    @Test
    public void testCancelOrder(){
        NetSiteOrder order = orderService.cancelOrder("15117");
        System.out.println(JSONObject.toJSONString(order));
    }

    @Test
    public void testSelectInfoByOnlyCode(){
        NetSiteInfoDto result =  bannerService.selectInfoByOnlyCode("0090B2B6215D");
        System.out.println(JSONObject.toJSONString(result));

        List<NetSiteBillRule> rules = ruleService.listNetSiteBillRule(4l);
        System.out.println(JSONObject.toJSONString(rules));


        List<NetSiteOrder> list = orderService.listByTypeAndStatus(OrderStatus.USEING);
        System.out.println(JSONObject.toJSONString(list));


    }



    @Test
    public void testCheckNetSiteOnline(){
        netSiteService.checkNetSiteOnline();
    }

    @Test
    public void testPageRechargeRecord(){
        SimplePageDto<RechargeRecordDto> res = userMoneyRecordService.pageRechargeRecord(2841l, 1, 10);
        System.out.println(JSONObject.toJSONString(res));
    }

    @Test
    public void testOrderCheck(){
        PageData<NetSiteUserReqDto> pageData = new PageData<>();
        NetSiteUserReqDto reqDto = new NetSiteUserReqDto();
        reqDto.setStartDate("2025-09-08 00:00:00");
        reqDto.setEndDate("2025-09-14 00:00:00");
        pageData.setBody(reqDto);
        pageData.setPageSize(1000);
        SimplePageDto<NetSiteOrderResDto> resDto = orderService.queryNetSiteOrderByPage(pageData);
        List<NetSiteOrderResDto> list = resDto.getDatas();
        for (NetSiteOrderResDto res : list) {
            NetSiteOrder order = orderService.selectByPayCode(res.getPayCode());
            BigDecimal original = order.getTotalAmount();
            orderSettlementService.calculateRemainingBalance(order);
            if(order.getTotalAmount().compareTo(original) > 0){
                System.out.println(res.getMobile() + "   " + order.getId() + " " + order.getTotalAmount()  +  "   " + original + "  " + res.getMobile());
            }
        }

    }


    @Test
    public void testSaveAndUpdate(){
        NetSiteOrderDetail detail = new NetSiteOrderDetail();
        detail.setType(1);
        detail.setBeginTime(DateUtils.curTime());
        detail.setOrderId(15000l);
        detail.setEndTime(DateUtils.curTimeAddSeconds(10));
        detailService.save(detail);
        NetSiteOrderDetail detail2 = detailService.openDetail(15000l, 01);
        long second = DateUtils.secondsDifference(DateUtils.curTime(), detail2.getBeginTime());
        if (detail2 != null && second >= 10) {
            detail2.setEndTime(DateUtils.curTime());
            detailService.update(detail2);
        }
    }
}
