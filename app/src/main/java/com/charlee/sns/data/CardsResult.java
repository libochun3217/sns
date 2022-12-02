package com.charlee.sns.data;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 消息列表返回结果
 * 注意：成员变量命名要和JSON对应。GSON转换规则为FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES
 */
public class CardsResult extends ResultBase {
    private final PagedList<CardItem> cards;

    public CardsResult(int errCode, @Nullable String errMsg, Boolean hasMore, @NonNull PagedList<CardItem> cards) {
        super(errCode, errMsg, hasMore);
        this.cards = cards;
    }

    @NonNull
    public PagedList<CardItem> getCards() {
        return cards;
    }

    @Override
    public boolean isValid() {
        return cards != null && cards.isValid();
    }

    @Override
    public void setServerTimeStamp(long serverTimeStamp) {
        super.setServerTimeStamp(serverTimeStamp);
        if (cards != null) {
            cards.setUpdateTime(serverTimeStamp);
        }
    }
}
