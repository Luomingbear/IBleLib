package com.lingsir.iblelib;

import java.util.UUID;

/**
 * uuid列表
 * Created by luoming on 2017/11/28.
 */

public class UUIDUtil {
    //蓝牙串口服务
    public static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //
    public static final UUID LANAccessUsingPPPServiceClass_UUID = UUID.fromString("00001102-0000-1000-8000-00805F9B34FB");

    //文件传输服务
    public static final UUID OBEXFileTransferServiceClass_UUID = UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");
    public static final UUID IrMCSyncCommandServiceClass_UUID = UUID.fromString("00001107-0000-1000-8000-00805F9B34FB");

    //蓝牙打印服务
    public static final UUID HCRPrintServiceClass_UUID = UUID.fromString("00001126-0000-1000-8000-00805F9B34FB");
    public static final UUID HCRScanServiceClass_UUID = UUID.fromString("00001127-0000-1000-8000-00805F9B34FB");


}
