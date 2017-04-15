package com.huangmei.interfaces.monitor.trusteeship;

import com.huangmei.commonhm.manager.operate.CanDoOperate;
import com.huangmei.commonhm.manager.operate.Operate;
import com.huangmei.commonhm.model.Room;
import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;
import com.huangmei.interfaces.monitor.MonitorTask;
import com.huangmei.interfaces.websocket.Mapping.ActionRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 托管任务
 */
public class TrusteeshipTask implements MonitorTask {

    private static final Logger log = LoggerFactory.getLogger(TrusteeshipTask.class);

    private ActionRouter actionRouter;

    private CanDoOperate waitingClientOperate;

    private MahjongGameData mahjongGameData;

    private Room room;

    private User user;

    @Override
    public String getTaskName() {
        return "托管任务";
    }

    @Override
    public void run() {
        try {
            // 有过操作，则过，无过操作，则打出一张牌
            if (waitingClientOperate.getOperates().contains(Operate.GUO)) {
                log.info(
                        "玩家[id={}]正在托管中,可以执行[{}]，自动为其[过]",
                        waitingClientOperate.getRoomMember().getUserId(),
                        waitingClientOperate.toOperateNameString()
                );
                actionRouter.handleGuo(room, user);
            } else if (waitingClientOperate.getOperates().contains(Operate.PLAY_A_MAHJONG)) {
                PersonalCardInfo personalCardInfo = PersonalCardInfo.getPersonalCardInfo(mahjongGameData.getPersonalCardInfos(), user);
                actionRouter.handlePlayACard(room, user, personalCardInfo.getTouchMahjong());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    @Override
    public void setSuccessCallback(Runnable success) {

    }

    @Override
    public void setFailCallback(Runnable fail) {

    }

    @Override
    public void setFinishCallback(Runnable finish) {

    }

    public static class Builder {

        private TrusteeshipTask task;

        public Builder() {
            task = new TrusteeshipTask();
        }

        public Builder setActionRouter(ActionRouter actionRouter) {
            task.actionRouter = actionRouter;
            return this;
        }

        public Builder setCanDoOperate(CanDoOperate waitingClientOperate) {
            task.waitingClientOperate = waitingClientOperate;
            return this;
        }

        public Builder setMahjongGameData(MahjongGameData mahjongGameData) {
            task.mahjongGameData = mahjongGameData;
            return this;
        }

        public Builder setRoom(Room room) {
            task.room = room;
            return this;
        }

        public Builder setUser(User user) {
            task.user = user;
            return this;
        }

        public TrusteeshipTask build() {
            return task;
        }
    }
}
