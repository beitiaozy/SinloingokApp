package com.sinloingok.app.service.user;

import java.math.BigDecimal;
import java.util.List;

import com.sinloingok.app.dao.UserAddressAccountDao;
import com.sinloingok.app.dtos.SimplePageDto;
import com.sinloingok.app.dtos.UserAccountDto;
import com.sinloingok.app.models.fund.UserAddressAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.sinloingok.app.dao.UserDao;
import com.sinloingok.app.models.user.User;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAddressAccountDao userAccountDao;

    public User findById(Long id, long addressId) {
        return userDao.selectById(id, addressId);
    }

    public SimplePageDto<User> memberList(UserAccountDto dto, Integer offset, Integer pageSize){
        String mobile = dto.getMobile();
        List<User> list = userDao.selectByMobileLike(mobile, dto.getAddressId(), offset, pageSize);
        long count = userDao.countByMobileLike(mobile, dto.getAddressId());
        return new SimplePageDto<>(offset, pageSize, count, list);
    }

    public User selectByUniqueKey(String openId, long addressId) {
        return userDao.selectByUniqueKey(openId, addressId);
    }

    public int updateMoney(Long id, float balance, float extMoney) {
       return userDao.updateMoney(id, balance, extMoney);
    }

    public void updateUserTotalBalance(User user) {
        updateUserAccount(user);
        BigDecimal balance = userAccountDao.sumBalanceByUser(user.getId());
        BigDecimal ext = userAccountDao.sumExtMoneyByUser(user.getId());
        userDao.updateMoney(user.getId(), balance.floatValue(), ext.floatValue());
    }

    public void insert(User user) {
        userDao.insertOrUpdateUser(user);
        updateUserAccount(user);
    }
    public void update(User user) {
        userDao.update(user);
        updateUserAccount(user);
    }

    private void updateUserAccount(User user){
        if(user.getCurrentAccount() != null){
            userAccountDao.insertOrUpdateUserAccount(user.getCurrentAccount());
        }
    }
}
