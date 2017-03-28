package com.huangmei.commonhm.manager.picker;

import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.model.mahjong.MahjongGameData;
import com.huangmei.commonhm.model.mahjong.PersonalCardInfo;

import java.util.List;

/**
 * 从mahjongGameData中提取需要进行判断的PersonalCardInfo
 */
public interface PersonalCardInfoPicker {

    List<PersonalCardInfo> pick(MahjongGameData mahjongGameData, User user);
}
