package com.huangmei.commonhm.model.mahjong.vo;


import java.util.List;

/**
 * 单局结算
 */
public class SingleScoreVo {

    List<SingleUserScoreVo> singleUserScoreVos;

    public List<SingleUserScoreVo> getCountInfos() {
        return singleUserScoreVos;
    }

    public void setSingleUserScoreVos(List<SingleUserScoreVo> singleUserScoreVos) {
        this.singleUserScoreVos = singleUserScoreVos;
    }
}
