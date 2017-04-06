package com.huangmei.commonhm.manager.qiangGang;

import com.huangmei.commonhm.manager.AbstractManager;
import com.huangmei.commonhm.manager.picker.PutOutCardPicker;
import com.huangmei.commonhm.manager.scanTask.impl.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 自己明杠，扫描其他玩家有没有抢杠
 * 依次扫描scanTasks中的具体任务，得出所有玩家可以有的操作列表
 */
@Component
public class QiangGangManager extends AbstractManager implements InitializingBean {

    @Override
    protected void setPersonalCardInfoPicker() {
        this.personalCardInfoPicker = new PutOutCardPicker();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scanTasks = new ArrayList<>();

        // 硬胡
        scanTasks.add(ChiYingPengPengHu.class);
        scanTasks.add(ChiYingQiDuiHu.class);
        scanTasks.add(ChiYingPingHu.class);

        // 软胡
        scanTasks.add(ChiRuanPengPengHu.class);
        scanTasks.add(ChiRuanQiDuiHu.class);
        scanTasks.add(ChiRuanPingHu.class);

        setPersonalCardInfoPicker();
    }
}
