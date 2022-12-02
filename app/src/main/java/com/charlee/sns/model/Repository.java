package com.charlee.sns.model;

import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.charlee.sns.data.DataModelBase;

/**
 * 模型层对象池。仅供模型层内部使用。
 */
abstract class Repository<ModelTypeT, DataLayerT extends DataModelBase> {

    // 弱引用，ID无引用时映射会被移除
    private final WeakHashMap<String, ModelTypeT> objectPool;
    // WeakHashMap不是线程安全的，需要加锁
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * 构造函数
     * @param iniSize     初始大小
     */
    public Repository(int iniSize) {
        objectPool = new WeakHashMap<>(iniSize);
    }

    /**
     * 从数据层对象获取模型层对象
     * @param data      数据层对象
     * @return          模型层对象
     */
    @NonNull
    public ModelTypeT getModelByData(@NonNull DataLayerT data) {
        String id = data.getId();
        rwLock.readLock().lock();
        try {
            ModelTypeT found = objectPool.get(id);
            if (found == null) {
                rwLock.readLock().unlock();
                rwLock.writeLock().lock();
                try {
                    // 再次检查
                    found = objectPool.get(id);
                    if (found == null) {
                        found = create(data);
                        objectPool.put(id, found);
                    }
                } finally {
                    // 释放写锁前降级到读锁
                    rwLock.readLock().lock();
                    rwLock.writeLock().unlock();
                }
            } else {
                updateModel(found, data);
            }

            return found;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 根据ID获取模型层对象
     * @param id        用户ID
     * @return          模型层对象。找不到则返回null。
     */
    @Nullable
    public ModelTypeT getModelById(@NonNull String id) {
        rwLock.readLock().lock();
        try {
            return objectPool.get(id);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * 确保缓存中存在模型对象
     * @param model     模型对象
     */
    public void assureModel(@NonNull ModelTypeT model) {
        String id = getModelId(model);
        rwLock.readLock().lock();
        try {
            ModelTypeT found = objectPool.get(id);
            if (found == null) {
                rwLock.readLock().unlock();
                rwLock.writeLock().lock();
                try {
                    // 再次检查
                    found = objectPool.get(id);
                    if (found == null) {
                        objectPool.put(id, model);
                    }
                } finally {
                    // 释放写锁前降级到读锁
                    rwLock.readLock().lock();
                    rwLock.writeLock().unlock();
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    protected abstract String getModelId(@NonNull ModelTypeT model);
    protected abstract ModelTypeT create(@NonNull DataLayerT data);
    protected abstract void updateModel(@NonNull ModelTypeT model, @NonNull DataLayerT data);
}
