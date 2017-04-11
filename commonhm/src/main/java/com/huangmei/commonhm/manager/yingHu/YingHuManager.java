package com.huangmei.commonhm.manager.yingHu;

import com.huangmei.commonhm.manager.AbstractManager;
import com.huangmei.commonhm.manager.picker.GetACardPicker;
import com.huangmei.commonhm.manager.scanTask.impl.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * 扫描是否可以硬自摸
 */
@Component
public class YingHuManager extends AbstractManager implements InitializingBean {

    @Override
    protected void setPersonalCardInfoPicker() {
        this.personalCardInfoPicker = new GetACardPicker();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scanTasks = new ArrayList<>();

        // 硬胡
        scanTasks.add(ZiMoYingPengPengHu.class);
        scanTasks.add(ZiMoYingQiDuiHu.class);
        scanTasks.add(ZiMoYingPingHu.class);

        setPersonalCardInfoPicker();
    }
}
