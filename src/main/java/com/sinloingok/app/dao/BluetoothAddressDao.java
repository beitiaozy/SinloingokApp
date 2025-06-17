package com.sinloingok.app.dao;

import java.util.List;

import com.sinloingok.app.dtos.BluetoothAddressResDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.sinloingok.app.models.bluetooth.BluetoothAddress;

@Mapper
public interface BluetoothAddressDao {
    List<BluetoothAddress> selectByUserId(@Param("userId") Long userId);
    BluetoothAddress selectByUserIdAndName(@Param("userId") Long userId,
                                           @Param("addrName") String addrName);
    BluetoothAddress selectById(@Param("id") Long id);

    int insert(BluetoothAddress address);

    int update(BluetoothAddress address);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("updateTime") String updateTime);

    List<BluetoothAddressResDto> queryBluetoothAddressList();
}
