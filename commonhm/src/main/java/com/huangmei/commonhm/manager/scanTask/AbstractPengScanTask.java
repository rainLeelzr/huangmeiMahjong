package com.huangmei.commonhm.manager.scanTask;


import com.huangmei.commonhm.model.mahjong.BaseOperate;

/**
 * 扫描用户是否可以碰
 */
public abstract class AbstractPengScanTask extends ScanTask {

    @Override
    public BaseOperate getBaseOperate() {
        return BaseOperate.PENG;
    }
}
