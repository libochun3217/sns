package com.charlee.sns.model;


import androidx.annotation.NonNull;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.data.CardItem;

import java.security.InvalidParameterException;

/**
 * 卡片页的model对象
 */
public class Card extends ModelBase {
    public static final int NORMAL_MESSAGE = 0;
    public static final int VIDEO_MESSAGE = 1;
    public static final int OFFICIAL_MESSAGE = 3;
    public static final int RECOMMEND_LIST = 4;
    public static final int RECOMMEND_GRID = 5;
    public static final int HEADER_CARD = 1000;
    public static final int FOOTER_CARD = 1001;

    private final IMotuSns motuSns;
    protected final IModelRepository repository;

    private CardItem cardItem;
    private UserMessage message;

    // 仅供CardRepository调用以保证每个卡片只有唯一实例
    Card(@NonNull IMotuSns motuSns, @NonNull IModelRepository repository, CardItem cardItem) {
        if (BuildConfig.DEBUG) {
            if (repository.getCardById(cardItem.getId()) != null) {
                throw new InvalidParameterException("DO NOT create a different instance for the same ID!");
            }
        }
        this.motuSns = motuSns;
        this.repository = repository;
        this.cardItem = cardItem;

        if (cardItem.getMessage() != null) {
            message = repository.getMessageByData(cardItem.getMessage());
        }

        itemId = Long.getLong(this.cardItem.getId(), this.cardItem.getId().hashCode());
    }

    public String getId() {
        return cardItem.getId();
    }

    public int getType() {
        return cardItem.getType();
    }

    public UserMessage getMessage() {
        return message;
    }

    synchronized void update(@NonNull final CardItem other) {
        if (other == null) {
            if (BuildConfig.DEBUG) {
                throw new InvalidParameterException("Update Comment with null object!");
            } else {
                return;
            }
        }

        boolean updated;
        if (cardItem == null) {
            this.cardItem = other;
            updated = true;
        } else {
            updated = cardItem.update(other);
        }

        if (updated) {
            if (cardItem.getMessage() != null) {
                message = repository.getMessageByData(cardItem.getMessage());
            }
        }
    }

}
