package com.sinloingok.app.service.notice;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sinloingok.app.dao.PlatformNoticeDao;
import com.sinloingok.app.models.activity.PlatformNotice;

@Service
public class PlatformNoticeService {
    @Autowired
    private PlatformNoticeDao platformNoticeDao;

    public List<PlatformNotice> activeList() {
        return platformNoticeDao.selectActive();
    }
}
