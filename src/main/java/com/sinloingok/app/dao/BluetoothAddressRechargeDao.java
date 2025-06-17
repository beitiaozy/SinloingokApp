package com.sinloingok.app.dao;

import com.sinloingok.app.models.bluetooth.BluetoothAddressRecharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BluetoothAddressRechargeDao {
    List<BluetoothAddressRecharge> rechargeListWithNewRecord(@Param("expirationDate") String expirationDate, @Param("addressId") long addressId);

    BluetoothAddressRecharge selectById(@Param("id") Long id);

    int insertBluetoothAddressRecharge(@Param("bean") BluetoothAddressRecharge bean);

    int updateBluetoothAddressRecharge(@Param("bean") BluetoothAddressRecharge bean);

    void deleteById(@Param("rechargeId") Long id);
}
