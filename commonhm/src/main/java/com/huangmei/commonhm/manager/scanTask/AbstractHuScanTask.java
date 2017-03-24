package com.huangmei.commonhm.manager.scanTask;


import com.huangmei.commonhm.model.mahjong.BaseOperate;

/**
 * 扫描用户是否可以吃胡
 */
public abstract class AbstractHuScanTask extends ScanTask {

    @Override
    public BaseOperate getBaseOperate() {
        return BaseOperate.HU;
    }
}
