package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.operate.Operate;

/**
 * 扫描是否可以硬暗杠
 */
public class YingAnGang extends YingDaMingGang {

    @Override
    public Operate getOperate() {
        return Operate.YING_AN_GANG;
    }

}
