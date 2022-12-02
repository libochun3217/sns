package com.charlee.sns.adapter;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.charlee.sns.BuildConfig;
import com.charlee.sns.model.ICollectionObserver;
import com.charlee.sns.model.IObservable;
import com.charlee.sns.model.IPageableList;
import com.charlee.sns.model.ModelBase;

import bolts.Task;

/**
 */
public abstract class PageableListAdapter<ItemTypeT extends ModelBase, ViewHolderT extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<ViewHolderT> {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    protected final IPageableList<ItemTypeT> itemListModel;
    protected List<ItemTypeT> itemList = new ArrayList<>(); // 保存一份列表，通过通知来更新以避免不同步导致的崩溃

    private boolean isAttachedToRecyclerView;

    // 用于在Adapter从Recycler上detach后保存数据变化通知以便在attach之后重新发出
    private class NotificationItem {
        public ICollectionObserver.Action action;
        public ItemTypeT itemModified;
        public List<Object> range;

        public NotificationItem(ICollectionObserver.Action action, ItemTypeT itemModified, List<Object> range) {
            this.action = action;
            this.itemModified = itemModified;
            this.range = range;
        }
    }

    private List<NotificationItem> notificationList = new ArrayList<>();

    private ICollectionObserver observer = new ICollectionObserver() {
        @Override
        public void update(final IObservable<ICollectionObserver> observable,
                           final Action action,
                           final Object item,
                           final List<Object> range) {
            // 在UI线程处理通知
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    onUpdate(observable, action, item, range);
                }
            });
        }
    };

    public PageableListAdapter(@NonNull IPageableList<ItemTypeT> items) {
        this.setHasStableIds(true);
        this.itemListModel = items;
        if (!itemListModel.isEmpty()) {
            for (int i = 0; i < itemListModel.size(); ++i) {
                itemList.add(itemListModel.get(i));
            }
        }

        itemListModel.addObserver(observer);
    }

    // 和setHasStableIds(true)配合
    @Override
    public long getItemId(int position) {
        ItemTypeT item = itemList.get(position);
        return item != null ? item.getItemId() : super.getItemId(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        isAttachedToRecyclerView = true;
        processNotificationList();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        isAttachedToRecyclerView = false;
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * 获取子元素在列表中的位置。直接用itemList.indexOf会导致在HeaderedListAdapter及其子类中出错。
     * @param item  子元素
     * @return      子元素对象在列表中的索引值。不存在列表中则返回-1。
     */
    public int indexOfItem(ItemTypeT item) {
        return itemList.indexOf(item);
    }

    public Task<Boolean> refresh() {
        try {
            return itemListModel.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Task.forResult(false);
    }

    public Task<Boolean> loadNextPage() {
        try {
            return itemListModel.loadNextPage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Task.forResult(false);
    }

    /**
     * 供子类处理列表变化的通知
     * @return 如果已经处理（notify...已经发出）则返回true，否则返回false
     */
    protected boolean onListChanged(final ICollectionObserver.Action action,
                                    final ItemTypeT item, final List<Object> range) {
        return false;
    }

    // 处理保存的观察者通知列表
    private void processNotificationList() {
        if (!isAttachedToRecyclerView) {
            return;
        }

        if (!notificationList.isEmpty()) {
            for (NotificationItem notificationItem : notificationList) {
                handleChange(notificationItem.action, notificationItem.itemModified, notificationItem.range);
            }

            notificationList.clear();
        }
    }

    // 用于在UI线程处理来自被观察者的更新
    private void onUpdate(final IObservable observable,
                          final ICollectionObserver.Action action,
                          final Object data,
                          final List<Object> range) {
        handleChange(action, (ItemTypeT) data, range);
    }

    private void handleChange(final ICollectionObserver.Action action, final ItemTypeT item, final List<Object> range) {
        if (BuildConfig.DEBUG) {
            Log.i("PageableListAdapter", "handleChange: " + action.toString()
                    + " Adapter: " + getClass().getSimpleName() + " List: " + itemListModel.getClass().getSimpleName());
            if (Looper.getMainLooper() != Looper.myLooper()) {
                throw new InvalidParameterException("Not called from UI thread!");
            }
        }

        if (isAttachedToRecyclerView) {
            if (onListChanged(action, item, range)) {
                return;
            }

            if (action == ICollectionObserver.Action.Clear) {
                if (!itemList.isEmpty()) {
                    itemList.clear();
                    if (onListChanged(action, item, range)) {
                        notifyItemRangeRemoved(0, itemList.size());
                    }
                }
            } else if (action == ICollectionObserver.Action.AppendRange) {
                if (range != null && !range.isEmpty()) {
                    int newStart = itemList.size();
                    for (int i = 0; i < range.size(); ++i) {
                        itemList.add((ItemTypeT) range.get(i));
                    }

                    if (newStart == 0) {
                        notifyDataSetChanged(); // 不发送此通知界面不会更新
                    } else {
                        notifyItemRangeInserted(newStart, range.size());
                    }
                } else if (BuildConfig.DEBUG) {
                    throw new InvalidParameterException("AppendRange");
                }
            } else {
                switch (action) {
                    case AddItemToFront:
                        if (item != null) {
                            itemList.add(0, item);
                            int indexToAdd = indexOfItem(item); // 考虑HeaderedListAdapter的特殊情况
                            notifyItemInserted(indexToAdd);
                        } else if (BuildConfig.DEBUG) {
                            throw new InvalidParameterException("AddItemToFront");
                        }
                        break;
                    case RemoveItem:
                        int indexToRemove = indexOfItem(item);
                        if (itemList.remove(item)) {
                            if (itemList.isEmpty()) {
                                notifyDataSetChanged(); // 不发送此通知界面不会更新
                            } else {
                                notifyItemRemoved(indexToRemove);
                            }
                        } else if (BuildConfig.DEBUG) {
                            throw new InvalidParameterException("RemoveItem");
                        }
                        break;
                    case UpdateItem:
                        int indexToUpdate = indexOfItem(item);
                        if (indexToUpdate >= 0) {
                            notifyItemChanged(indexToUpdate);
                        } else if (BuildConfig.DEBUG) {
                            throw new InvalidParameterException("UpdateItem");
                        }
                        break;
                    default:
                        break;
                }
            }
        } else {
            notificationList.add(new NotificationItem(action, item, range));
        }
    }
}
