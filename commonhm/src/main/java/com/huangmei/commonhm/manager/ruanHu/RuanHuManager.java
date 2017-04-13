package com.huangmei.commonhm.manager.ruanHu;

import com.huangmei.commonhm.manager.AbstractManager;
import com.huangmei.commonhm.manager.picker.GetACardPicker;
import com.huangmei.commonhm.manager.scanTask.impl.ZiMoRuanPengPengHu;
import com.huangmei.commonhm.manager.scanTask.impl.ZiMoRuanPingHu;
import com.huangmei.commonhm.manager.scanTask.impl.ZiMoRuanQiDuiHu;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 扫描是否可以软自摸
 */
@Component
public class RuanHuManager extends AbstractManager implements InitializingBean {

    @Override
    protected void setPersonalCardInfoPicker() {
        this.personalCardInfoPicker = new GetACardPicker();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scanTasks = new ArrayList<>();

        // 硬胡
        scanTasks.add(ZiMoRuanPengPengHu.class);
        scanTasks.add(ZiMoRuanQiDuiHu.class);
        scanTasks.add(ZiMoRuanPingHu.class);

        setPersonalCardInfoPicker();
    }
}
