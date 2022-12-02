package com.charlee.sns.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.data.DataModelBase;
import com.charlee.sns.data.PagedList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * 可分页列表接口实现类。实现IPageableList接口，进行分页列表的合并。
 * 实现IObservable接口。当列表有变化或者列表中的项目有变化时观察者会得到通知。
 */
public abstract class PageableList<ModelTypeT extends ModelBase, DataLayerT extends DataModelBase> extends
        CollectionObservableBase
        implements
        IPageableList<ModelTypeT> {
    protected final IMotuSns motuSns;
    protected final IModelRepository repository;

    private final PagingType pagingType;
    private int totalSize;
    private String lastId = IMotuSns.FIRST_PAGE_ID;
    private List<ModelTypeT> itemList = Collections.synchronizedList(new LinkedList<ModelTypeT>());
    private List<PagedList<DataLayerT>> resultList =
            Collections.synchronizedList(new LinkedList<PagedList<DataLayerT>>());

    private IObserver itemObserver = new IObserver() {
        @Override
        public void update(IObservable observable, Object data) {
            ModelTypeT model = (ModelTypeT) observable;
            if (model != null && itemList.contains(model)) {
                if (model.isRemoved()) {
                    remove(model);
                } else if (model.isFiltered()) {
                    filter(model);
                } else {
                    setChanged();
                    notifyObservers(ICollectionObserver.Action.UpdateItem, observable, null);
                }
            }
        }
    };

    /**
     * 分页类型
     */
    public enum PagingType {
        IndexBased, // 基于索引
        IdBased     // 基于ID
    }

    /**
     * 构造函数
     *
     * @param totalSize  总元素个数，不确定时使用TOTAL_SIZE_INFINITE
     * @param pagingType 分页方式
     */
    public PageableList(@NonNull final IMotuSns motuSns,
                        @NonNull final IModelRepository repository,
                        int totalSize, PagingType pagingType) {
        this.motuSns = motuSns;
        this.repository = repository;
        this.totalSize = totalSize;
        this.pagingType = pagingType;
    }

    /**
     * 构造函数
     *
     * @param totalSize  总元素个数，不确定时使用TOTAL_SIZE_INFINITE
     * @param pagingType 分页方式
     * @param firstPage  第一页的列表
     */
    public PageableList(@NonNull final IMotuSns motuSns,
                        @NonNull final IModelRepository repository,
                        int totalSize, PagingType pagingType, PagedList<DataLayerT> firstPage) {
        this.motuSns = motuSns;
        this.repository = repository;
        this.totalSize = totalSize;
        this.pagingType = pagingType;

        if (firstPage != null && firstPage.isValid()) {
            resultList.add(firstPage);
            List<DataLayerT> data = firstPage.getData();
            if (data != null && !data.isEmpty()) {
                addToItemList(data);
            }
        }
    }

    @Nullable
    @Override
    public ModelTypeT get(int location) {
        return itemList.get(location);
    }

    @Override
    public void add(ModelTypeT newModel) {
        itemList.add(0, newModel);
        ++totalSize;
        newModel.addObserver(itemObserver);
        setChanged();
        notifyObservers(ICollectionObserver.Action.AddItemToFront, newModel, null);
    }

    @Override
    public boolean remove(ModelTypeT modelToRemove) {
        if (itemList.remove(modelToRemove)) {
            --totalSize;
            modelToRemove.deleteObserver(itemObserver);
            if (!modelToRemove.isRemoved()) {
                modelToRemove.setRemoved();
            }

            setChanged();
            notifyObservers(ICollectionObserver.Action.RemoveItem, modelToRemove, null);
            return true;
        }

        return false;
    }

    @Override
    public boolean filter(@Nullable ModelTypeT modelToFilter) {
        if (itemList.remove(modelToFilter)) {
            --totalSize;
            modelToFilter.deleteObserver(itemObserver);

            setChanged();
            notifyObservers(ICollectionObserver.Action.RemoveItem, modelToFilter, null);
            return true;
        }

        return false;
    }

    @Override
    public void clear() {
        if (itemList != null) {
            itemList.clear();
            setChanged();
            notifyObservers(ICollectionObserver.Action.Clear, null, null);
        }
    }

    @Override
    public int indexOf(ModelTypeT e) {
        return itemList.indexOf(e);
    }

    @Override
    public boolean isEmpty() {
        return itemList.isEmpty();
    }

    @Override
    public int size() {
        return itemList.size();
    }

    @Override
    public int getTotalSize() {
        return totalSize;
    }

    @Override
    public String getLastId() {
        return lastId;
    }

    @Override
    public Task<Boolean> refresh() throws Exception {
        return getFirstPage().onSuccess(new Continuation<PagedList<DataLayerT>, Boolean>() {
            @Override
            public Boolean then(Task<PagedList<DataLayerT>> task) throws Exception {
                PagedList<DataLayerT> result = task.getResult();
                if (result == null || !result.isValid()) {
                    return false;
                }

                lastId = result.getLastId();

                List<DataLayerT> data = result.getData();
                if (data != null) {
                    for (ModelTypeT model : itemList) {
                        model.deleteObserver(itemObserver);
                    }

                    setChanged();
                    notifyObservers(ICollectionObserver.Action.Clear, null, null);
                    itemList.clear();
                    resultList.clear();
                    resultList.add(result);
                    addToItemList(data);
                    return true;
                }

                return false;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    @Override
    public boolean hasNextPage() {
        if (resultList.isEmpty()) {
            return false;
        }

        return resultList.get(resultList.size() - 1).hasMore();
    }

    @Override
    public Task<Boolean> loadNextPage() throws Exception {
        if (resultList.isEmpty() || !hasNextPage()) {
            return Task.forResult(false);
        }

        return getNextPage(resultList.get(resultList.size() - 1))
                .onSuccess(new Continuation<PagedList<DataLayerT>, Boolean>() {
                    @Override
                    public Boolean then(Task<PagedList<DataLayerT>> task) throws Exception {
                        PagedList<DataLayerT> result = task.getResult();
                        List<DataLayerT> data = result != null ? result.getData() : null;
                        if (data != null && !data.isEmpty()) {
                            resultList.add(result);

                            List<DataLayerT> listToAdd = new ArrayList<>();
                            // 基于ID的分页，不需要去重。基于序号的分页需要去重。
                            if (pagingType == PagingType.IndexBased) {
                                for (DataLayerT item : data) {
                                    if (!item.isValid()) { // 忽略无需数据
                                        continue;
                                    }

                                    boolean found = false;
                                    for (ModelTypeT old : itemList) {
                                        if (hasSameId(old, item)) {
                                            found = true;
                                            break;
                                        }
                                    }

                                    if (!found) {
                                        listToAdd.add(item);
                                    }
                                }
                            } else {
                                for (DataLayerT item : data) {
                                    if (!item.isValid()) { // 忽略无需数据
                                        continue;
                                    }
                                    listToAdd.add(item);
                                }
                            }

                            addToItemList(listToAdd);

                            return true;
                        }

                        return false;
                    }
                }, Task.UI_THREAD_EXECUTOR);
    }

    private void addToItemList(List<DataLayerT> data) {
        List<Object> addedList = new ArrayList<>();
        for (DataLayerT item : data) {
            if (item.isValid()) { // 忽略无需数据
                ModelTypeT model = addToItemList(item);
                if (model != null) {
                    addedList.add(model);
                }
            }
        }

        if (!addedList.isEmpty()) {
            setChanged();
            notifyObservers(ICollectionObserver.Action.AppendRange, null, addedList);
        }
    }

    // region Private Methods

    private ModelTypeT addToItemList(DataLayerT item) {
        ModelTypeT model = createModel(item);
        if (!model.isRemoved()) {
            model.addObserver(itemObserver);
            itemList.add(model);
            return model;
        }

        return null;
    }

    // endregion

    // region Abstract Methods to be implemented by child classes.

    /**
     * 获取第一页的数据
     *
     * @return 结果为列表的Task。列表可能为null。
     */
    @NonNull
    protected abstract Task<PagedList<DataLayerT>> getFirstPage();

    /**
     * 获取下一页的数据
     *
     * @param before 前一页的数据，用于在基于ID分页时获取lastId
     *
     * @return 结果为列表的Task。列表可能为null。
     */
    @NonNull
    protected abstract Task<PagedList<DataLayerT>> getNextPage(@NonNull PagedList<DataLayerT> before);

    /**
     * 从数据层对象创建模型层对象
     *
     * @param data 数据层对象
     *
     * @return 创建的模型层对象
     */
    protected abstract ModelTypeT createModel(@NonNull DataLayerT data);

    /**
     * 比较数据层对象和模型层对象是否具有相同ID，用于去重
     *
     * @param model 模型层对象
     * @param data  数据层对象
     *
     * @return 数据层对象和模型层对象具有相同ID则返回true，否则返回false。
     */
    protected abstract boolean hasSameId(@NonNull ModelTypeT model, @NonNull DataLayerT data);

    // endregion
}
