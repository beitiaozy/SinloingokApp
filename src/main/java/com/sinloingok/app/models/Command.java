package com.sinloingok.app.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 一条原子指令：对某设备 onlyCode 的某通道 channel 下发 OPEN/CLOSE，或用于结算侧的“虚拟指令” */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Command {
    private String onlyCode; // 设备编码
    private int channel;     // 通道 1..8
    private DeviceControl.Action command;  // "OPEN" / "CLOSE"

    public static Command of(String code, int ch, DeviceControl.Action cmd) {
        return new Command(code, ch, cmd);
    }
}
