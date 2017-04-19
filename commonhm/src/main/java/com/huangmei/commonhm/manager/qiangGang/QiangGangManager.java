package com.huangmei.commonhm.manager.qiangGang;

import com.huangmei.commonhm.manager.AbstractManager;
import com.huangmei.commonhm.manager.operate.CanDoOperate;
import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.manager.picker.PutOutCardPicker;
import com.huangmei.commonhm.manager.scanTask.impl.*;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.*;

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
    public List<CanDoOperate> scan(MahjongGameData mahjongGameData, Mahjong putOutMahjong, User user) throws IllegalAccessException, InstantiationException {
        List<CanDoOperate> canDoOperates = super.scan(mahjongGameData, putOutMahjong, user);
        if (canDoOperates.size() > 0) {
            // 将碰碰胡、七对胡、平胡的操作转换为抢杠胡
            CanDoOperate canDoOperate = new CanDoOperate();
            canDoOperate.setRoomMember(canDoOperates.get(0).getRoomMember());
            canDoOperate.setSpecialUserId(canDoOperates.get(0).getSpecialUserId());
            canDoOperate.setSpecialMahjong(canDoOperates.get(0).getSpecialMahjong());

            Set<Operate> operates = new TreeSet<>();
            operates.add(Operate.QIANG_GANG_HU);
            canDoOperate.setOperates(operates);

            canDoOperates = new ArrayList<>(1);
            canDoOperates.add(canDoOperate);
        }
        return canDoOperates;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        scanTasks = new ArrayList<>();

        // 硬胡
        scanTasks.add(ChiYingPengPengHu.class);
        scanTasks.add(ChiYingQiDuiHu.class);
        scanTasks.add(QiangGangYingPingHu.class);

        // 软胡
        scanTasks.add(ChiRuanPengPengHu.class);
        scanTasks.add(ChiRuanQiDuiHu.class);
        scanTasks.add(QiangGangRuanPingHu.class);

        setPersonalCardInfoPicker();
    }
}
