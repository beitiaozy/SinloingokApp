package com.sinloingok.app.constant;

public final class Endpoints {
    public static final String LOGIN                 = "https://auth.smyoo.com/v1/account/synloginopen";
    public static final String VERIFY_TICKET         = "https://auth.smyoo.com/api/gfriend/synloginticket";
    public static final String STATUS_CHANGED        = "https://auth.smyoo.com/api/gfriend/statuschanged";
    public static final String QUERY_DEVICES         = "https://auth.smyoo.com/api/gfriend/querydevices";
    public static final String QUERY_MCU_IDS         = "https://auth.smyoo.com/api/gfriend/querymcuids";
    public static final String GET_DEVICE_DATA       = "https://auth.smyoo.com/api/gfriend/getdevicedata";
    public static final String SET_DEVICE_DATA       = "https://auth.smyoo.com/api/gfriend/setdevicedata";
    public static final String SET_CHANNEL_DATA      = "https://auth.smyoo.com/api/gfriend/setchanneldata";
    public static final String SET_CHANNEL_DATA_AUTO = "https://auth.smyoo.com/api/gfriend/setchanneldataauto";
    public static final String SET_MULTI_CHANNELS    = "https://auth.smyoo.com/api/gfriend/setmultichannels";
    public static final String GET_MCU_INFO          = "https://auth.smyoo.com/api/gfriend/getmcuinfo";
    public static final String IR_GET_DATA           = "https://auth.smyoo.com/api/gfriend/irdevicegetdata";
    public static final String IR_SET_DATA           = "https://auth.smyoo.com/api/gfriend/irdevicesetdata";
    public static final String IR_SET_DATA_IRFILE    = "https://auth.smyoo.com/api/gfriend/irdevicesetdata_irfile";
    public static final String IR_DEVICE_INFO        = "https://auth.smyoo.com/api/gfriend/irdeviceinfo";
    public static final String IR_DEVICE_LIST        = "https://auth.smyoo.com/api/gfriend/irdevicelist";
    public static final String SPEAKER_LIST          = "https://auth.smyoo.com/api/gfriend/speakerlist";
    public static final String SPEAKER_ADD           = "https://auth.smyoo.com/api/gfriend/speakeradd";
    public static final String SPEAKER_DEL           = "https://auth.smyoo.com/api/gfriend/speakerdel";
    public static final String SPEAKER_PLAY          = "https://auth.smyoo.com/api/gfriend/speakerplay";
    public static final String SPEAKER_PLAY_TEXT     = "https://auth.smyoo.com/api/gfriend/speakerplaytext";
    public static final String SPEAKER_PLAY_LONGTEXT = "https://auth.smyoo.com/api/gfriend/speakerplaylongtext";
    public static final String SPEAKER_PLAY_FILE     = "https://auth.smyoo.com/api/gfriend/speakerplayfile";
    public static final String SPEAKER_PLAY_ONLINE   = "https://auth.smyoo.com/api/gfriend/speakerplayonline";
    public static final String SPEAKER_SD_FILE       = "https://auth.smyoo.com/api/gfriend/speakersdfile";
    public static final String GW_DEVICE_LIST        = "https://auth.smyoo.com/api/gfriend/gwdevicelist";
    private Endpoints() {}
}
