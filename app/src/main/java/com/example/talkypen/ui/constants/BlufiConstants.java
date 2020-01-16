package com.example.talkypen.ui.constants;

import java.util.UUID;

import blufi.espressif.params.BlufiParameter;

public final class BlufiConstants {
    public static final String BLUFI_PREFIX = "";

    public static final UUID UUID_SERVICE = BlufiParameter.UUID_SERVICE;
    public static final UUID UUID_WRITE_CHARACTERISTIC = BlufiParameter.UUID_WRITE_CHARACTERISTIC;
    public static final UUID UUID_NOTIFICATION_CHARACTERISTIC = BlufiParameter.UUID_NOTIFICATION_CHARACTERISTIC;

    public static final String KEY_BLE_DEVICE = "key_ble_device";

    public static final String KEY_CONFIGURE_PARAM = "configure_param";

    public static final int DEFAULT_MTU_LENGTH = 128;
    public static final int MIN_MTU_LENGTH = 15;
}