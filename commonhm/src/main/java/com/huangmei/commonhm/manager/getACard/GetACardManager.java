package com.huangmei.commonhm.manager.getACard;

import com.huangmei.commonhm.manager.AbstractManager;
import com.huangmei.commonhm.manager.picker.GetACardPicker;
import com.huangmei.commonhm.manager.scanTask.impl.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 自己摸到一张牌，可以的操作扫描管理器，
 * 如自摸、暗杠，加杠
 * 依次扫描scanTasks中的具体任务，得出所有玩家可以有的操作列表
 */
@Component
public class GetACardManager extends AbstractManager implements InitializingBean {

    @Override
    protected void setPersonalCardInfoPicker() {
        this.personalCardInfoPicker = new GetACardPicker();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scanTasks = new ArrayList<>();

        // 硬胡
        scanTasks.add(YingPengPengHu.class);
        scanTasks.add(YingQiDuiHu.class);
        scanTasks.add(YingPingHu.class);

        // 软胡
        scanTasks.add(RuanPengPengHu.class);
        scanTasks.add(RuanQiDuiHu.class);
        scanTasks.add(RuanPingHu.class);

        // 硬杠
        scanTasks.add(YingDaMingGang.class);

        // 硬碰
        scanTasks.add(YingPeng.class);

        // 软大明杠
        scanTasks.add(RuanDaMingGang.class);

        // 软碰
        scanTasks.add(RuanPeng.class);

        setPersonalCardInfoPicker();
    }
}
