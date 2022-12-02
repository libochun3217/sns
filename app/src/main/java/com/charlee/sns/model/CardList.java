package com.charlee.sns.model;


import androidx.annotation.NonNull;

import com.charlee.sns.data.CardItem;
import com.charlee.sns.data.CardsResult;
import com.charlee.sns.data.PagedList;

import bolts.Continuation;
import bolts.Task;

/**
 * 卡片列表
 */
public class CardList extends PageableList<Card, CardItem> {

    public CardList(@NonNull final IMotuSns motuSns,
                    @NonNull final IModelRepository repository,
                    int totalSize,
                    PagingType pagingType) {
        super(motuSns, repository, totalSize, pagingType);
    }

    protected Continuation<CardsResult, PagedList<CardItem>> firstPageParseCardsResultContinuation =
            new Continuation<CardsResult, PagedList<CardItem>>() {
                @Override
                public PagedList<CardItem> then(Task<CardsResult> task) throws Exception {
                    return getFilteredList(task.getResult().getCards(), true);
                }
            };

    protected Continuation<CardsResult, PagedList<CardItem>> parseCardsResultContinuation =
            new Continuation<CardsResult, PagedList<CardItem>>() {
                @Override
                public PagedList<CardItem> then(Task<CardsResult> task) throws Exception {
                    return getFilteredList(task.getResult().getCards(), false);
                }
            };

    private PagedList<CardItem> getFilteredList(PagedList<CardItem> itemList, boolean isFirstPage) {
        if (itemList == null) {
            return null;
        }

        for (int index = 0; index < itemList.getData().size(); index++) {
            CardItem item = itemList.getData().get(index);
            if (item.getType() == Card.OFFICIAL_MESSAGE) {
                item.setOfficialIndicator();
            }
        }

        // 判断是否是队列末尾
        if (itemList.hasMore() == false) {
            itemList.getData().add(new CardItem(Card.FOOTER_CARD));
        }
        return new PagedList<>(itemList.hasMore(), itemList.getLastId(), itemList.getData());
    }

    @Override
    protected Card createModel(@NonNull CardItem data) {
        return repository.getCardByData(data);
    }

    @Override
    protected boolean hasSameId(@NonNull Card model, @NonNull CardItem data) {
        return model.getId().equals(data.getId());
    }

    @NonNull
    @Override
    protected Task<PagedList<CardItem>> getFirstPage() {
        return motuSns.getCards(IMotuSns.FIRST_PAGE_ID, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(firstPageParseCardsResultContinuation);
    }

    @NonNull
    @Override
    protected Task<PagedList<CardItem>> getNextPage(@NonNull PagedList<CardItem> before) {
        String lastId = before.getLastId();
        if (lastId == null || lastId.isEmpty()) {
            return Task.forResult(null);
        }

        return motuSns.getCards(lastId, IMotuSns.DEFAULT_PAGE_SIZE)
                .onSuccess(parseCardsResultContinuation);
    }

}
