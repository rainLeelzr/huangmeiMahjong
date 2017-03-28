package com.huangmei.commonhm.manager;

import com.huangmei.commonhm.manager.operate.CanDoOperate;
import com.huangmei.commonhm.manager.picker.PersonalCardInfoPicker;
import com.huangmei.commonhm.manager.scanTask.BaseScanTask;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 操作扫描管理器，
 * * 依次扫描scanTasks中的具体任务，得出所有玩家可以有的操作列表
 */
public abstract class AbstractManager {

    /**
     * 已注册的扫描任务
     */
    protected List<Class<? extends BaseScanTask>> scanTasks;

    protected PersonalCardInfoPicker personalCardInfoPicker;

    protected abstract void setPersonalCardInfoPicker();

    public ArrayList<CanDoOperate> scan(MahjongGameData mahjongGameData, Mahjong putOutMahjong, User user) throws
            IllegalAccessException,
            InstantiationException {
        ArrayList<CanDoOperate> canDoOperates = new ArrayList<>();
        for (Class<? extends BaseScanTask> scanTask : scanTasks) {
            BaseScanTask task = scanTask.newInstance();
            task.setMahjongGameData(mahjongGameData);
            task.setPutOutMahjong(putOutMahjong);
            task.setUser(user);
            task.setCanOperates(canDoOperates);
            task.setPersonalCardInfoPicker(personalCardInfoPicker);
            task.scan();
        }
        Iterator<CanDoOperate> it = canDoOperates.iterator();
        while (it.hasNext()) {
            CanDoOperate next = it.next();
            if (next.getOperates().size() == 0) {
                it.remove();
            }
        }
        Collections.sort(canDoOperates);
        return canDoOperates;
    }


}
