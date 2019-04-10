package com.smart.smartbeauty.api;

/**
 * Created by deepglint on 2019/3/7.
 */

public enum  SmartResourceType {
    NONE("none", -1),       // 没有资源
    STICKER("sticker", 0),  // 贴纸资源类型
    FILTER("filter", 1),    // 滤镜资源类型
    EFFECT("effect", 2),    // 特效资源类型
    MAKEUP("makeup", 3),    // 彩妆资源类型
    MULTI("multi", 4);      // 多种类型混合起来

    private String name;
    private int index;

    SmartResourceType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
