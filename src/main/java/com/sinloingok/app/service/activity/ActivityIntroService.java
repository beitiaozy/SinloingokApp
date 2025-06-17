package com.sinloingok.app.service.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sinloingok.app.dao.ActivityIntroDao;
import com.sinloingok.app.models.activity.ActivityIntro;

@Service
public class ActivityIntroService {
    @Autowired
    private ActivityIntroDao activityIntroDao;

    public ActivityIntro latest() {
        return activityIntroDao.selectLatest();
    }

    public ActivityIntro findById(Long id) {
        return activityIntroDao.selectById(id);
    }
}
