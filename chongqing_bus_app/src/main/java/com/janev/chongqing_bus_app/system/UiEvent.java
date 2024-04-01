package com.janev.chongqing_bus_app.system;

public interface UiEvent {
    int EVENT_LINE_NAME = 1;//线路信息

    int EVENT_BROAD_SITE = 2;//报站

    int EVENT_SITE_LIST = 3;//站点列表

    int EVENT_SYNC_TIME = 4;//同步时间

    int EVENT_NEXT_SITE = 5;//下一站

    int EVENT_UPDATE_VOLUME = 6;//音量

    int EVENT_PLAY_VIDEO_ERROR = 7;//播放失败

    int EVENT_ADD_RES = 8;//添加播放资源

    int EVENT_RESET_RES = 9;//重置播放

    int EVENT_SCREEN_BROAD_SITE = 10;//屏幕报站

    int EVENT_LINE_STAR = 11;//线路星级

    int EVENT_WORKER_ID = 12;//驾驶员工号

    int EVENT_POLITIC = 13;//政治面貌

    int EVENT_ADD_MATERIAL = 15;//添加播放素材

    int EVENT_RESET_MATERIAL = 16;//重置播放素材

    int EVENT_SET_PULSE_FREQUENCY = 17;//设置心跳间隔

    int EVENT_QUERY_TCP_CONNECTION = 18;//查询TCP连接信息

    int EVENT_TCP_ADDRESS_CHANGED = 19;//TCP地址发生改变

    int EVENT_UPGRADE_APP = 20;//更新APP

    int EVENT_ADD_KNOWLEDGE = 21;//添加宣传语,list不为空则添加，list为空则查询后重设

    int EVENT_SHOW_TEMP_KNOWLEDGE = 22;//显示临时宣传语

    int EVENT_CONNECT_TCP = 23;//连接TCP

    int EVENT_ADD_MATERIAL_FINISH = 24;

    int UPDATE_RESOURCE = 25;
}
