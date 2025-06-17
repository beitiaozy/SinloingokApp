package com.sinloingok.app.service.netsite;

import com.sinloingok.app.dao.BluetoothAddressDao;
import com.sinloingok.app.dtos.BluetoothAddressResDto;
import com.sinloingok.app.models.bluetooth.BluetoothAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BluetoothAddressService {
    @Autowired
    private BluetoothAddressDao bluetoothAddressDao;

    public List<BluetoothAddress> listByUserId(Long userId) {
        return bluetoothAddressDao.selectByUserId(userId);
    }

    public BluetoothAddress findByUserIdAndName(Long userId, String addrName) {
        return bluetoothAddressDao.selectByUserIdAndName(userId, addrName);
    }

    public BluetoothAddress findById(Long id) {
        return bluetoothAddressDao.selectById(id);
    }

    public void insert(BluetoothAddress address) {
        bluetoothAddressDao.insert(address);
    }

    public void update(BluetoothAddress address) {
        bluetoothAddressDao.update(address);
    }

    public void updateStatus(Long id, String status, String updateTime) {
        bluetoothAddressDao.updateStatus(id, status, updateTime);
    }

    public List<BluetoothAddressResDto> queryBluetoothAddressList() {
        return bluetoothAddressDao.queryBluetoothAddressList();
    }
}
