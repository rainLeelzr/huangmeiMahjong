package com.huangmei.commonhm.manager.scanTask;


import com.huangmei.commonhm.model.mahjong.BaseOperate;

/**
 * 扫描用户是否可以杠
 */
public abstract class AbstractGangScanTask extends ScanTask {

    @Override
    public BaseOperate getBaseOperate() {
        return BaseOperate.GANG;
    }
}
