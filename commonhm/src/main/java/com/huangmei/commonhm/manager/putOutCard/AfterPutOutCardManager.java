package com.huangmei.commonhm.manager.putOutCard;

import com.huangmei.commonhm.manager.scanTask.impl.YingDaMingGang;
import com.huangmei.commonhm.manager.scanTask.impl.YingPeng;
import com.huangmei.commonhm.manager.scanTask.impl.YingPengPengHu;
import com.huangmei.commonhm.manager.scanTask.ScanTask;
import com.huangmei.commonhm.manager.scanTask.impl.YingQiDuiHu;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.Mahjong;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 别人出牌后其他玩家可以的操作扫描管理器，
 * 如吃胡、大明杠、碰
 * 依次扫描scanTasks中的具体任务，得出所有玩家可以有的操作列表
 */
@Component
public class AfterPutOutCardManager {

    /**
     * 已注册的扫描任务
     */
    private static List<Class<? extends ScanTask>> scanTasks;

    static {
        scanTasks = new ArrayList<>();
        scanTasks.add(YingPengPengHu.class);
        scanTasks.add(YingQiDuiHu.class);
        scanTasks.add(YingDaMingGang.class);
        scanTasks.add(YingPeng.class);
    }

    public ArrayList<AfterPutOutCardOperate> scan(MahjongGameData mahjongGameData, Mahjong putOutMahjong, User user) throws
            IllegalAccessException,
            InstantiationException {
        ArrayList<AfterPutOutCardOperate> afterPutOutCardOperates = new ArrayList<>();
        for (Class<? extends ScanTask> scanTask : scanTasks) {
            ScanTask task = scanTask.newInstance();
            task.setMahjongGameData(mahjongGameData);
            task.setPutOutMahjong(putOutMahjong);
            task.setUser(user);
            task.setCanOperates(afterPutOutCardOperates);
            task.scan();
        }
        return afterPutOutCardOperates;
    }


}
