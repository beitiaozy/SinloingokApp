package com.sinloingok.app.service.user;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sinloingok.app.dao.ManageUserDao;
import com.sinloingok.app.models.user.ManageUser;

@Service
public class ManageUserService {

    @Autowired
    private ManageUserDao manageUserDao;

    public ManageUser findByMobile(String mobile) {
        List<ManageUser> users = manageUserDao.queryByManageUser(new ManageUser(mobile, ""));
        return CollectionUtils.isNotEmpty(users) ? users.get(0) : null;
    }

    public ManageUser findByMobileAndPassword(String mobile, String password) {
        List<ManageUser> users = manageUserDao.queryByManageUser(new ManageUser(mobile, password));
        return CollectionUtils.isNotEmpty(users) ? users.get(0) : null;
    }

    public ManageUser findById(Long id) {
        return manageUserDao.selectById(id);
    }

    public List<String> findStatuses(Long applyUserId) {
        return manageUserDao.selectStatusesByApplyUserId(applyUserId);
    }

    public void update(ManageUser user) {
        manageUserDao.update(user);
    }
}
