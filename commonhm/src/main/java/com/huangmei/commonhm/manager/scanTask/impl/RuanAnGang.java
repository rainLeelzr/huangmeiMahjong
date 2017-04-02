package com.huangmei.commonhm.manager.scanTask.impl;

import com.huangmei.commonhm.manager.operate.Operate;

/**
 * 扫描是否可以软暗杠
 */
public class RuanAnGang extends RuanDaMingGang {

    @Override
    public Operate getOperate() {
        return Operate.RUAN_AN_GANG;
    }

}
