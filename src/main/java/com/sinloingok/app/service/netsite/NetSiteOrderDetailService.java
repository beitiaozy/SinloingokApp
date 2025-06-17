package com.sinloingok.app.service.netsite;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinloingok.app.dao.NetSiteOrderDetailDao;
import com.sinloingok.app.models.net4g.NetSiteOrderDetail;

@Service
public class NetSiteOrderDetailService {
    @Autowired
    private NetSiteOrderDetailDao netSiteOrderDetailDao;

    public NetSiteOrderDetail latestDetail(Long orderId, int type) {
        return netSiteOrderDetailDao.selectLatest(orderId, type);
    }

    public NetSiteOrderDetail openDetail(Long orderId, int type) {
        return netSiteOrderDetailDao.selectOpenDetail(orderId, type);
    }

    public java.util.List<NetSiteOrderDetail> listByOrderId(Long orderId) {
        return netSiteOrderDetailDao.selectByOrderId(orderId);
    }

    public void save(NetSiteOrderDetail detail) {
        netSiteOrderDetailDao.insert(detail);
    }

    public void update(NetSiteOrderDetail detail) {
        netSiteOrderDetailDao.update(detail);
    }
}
