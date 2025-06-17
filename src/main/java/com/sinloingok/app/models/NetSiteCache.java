package com.sinloingok.app.models;

import com.sinloingok.app.models.net4g.NetSite;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * NetSite 内存缓存（按 onlyCode 缓存 & 按 purpose 分类）
 *
 * - 支持 purpose：
 *   PM_NET_SITE（泡沫），WSC_NET_SITE（洗车机），KY_NET_SITE（空压机）
 * - 线程安全：读写锁。刷新时写锁独占，读操作在刷新完成前被阻塞，确保一致性。
 * - 刷新（refresh）：全量覆盖式重建索引，构建完成后一次性切换，保证读者看见的是完整快照。
 */
public final class NetSiteCache {

    private NetSiteCache() {}

    // ---------- 常量 ----------
    public static final String PURPOSE_PM  = "PM_NET_SITE";
    public static final String PURPOSE_WSC = "WSC_NET_SITE";
    public static final String PURPOSE_KY  = "KY_NET_SITE";

    // ---------- 主索引/分桶索引（受读写锁保护） ----------
    /** 主索引：onlyCode -> NetSite */
    private static Map<String, NetSite> MAIN = new ConcurrentHashMap<>();
    /** 分桶索引：purpose -> (onlyCode -> NetSite) */
    private static Map<String, Map<String, NetSite>> BUCKETS = new ConcurrentHashMap<>();
    private static Map<String, DeviceControl> deviceCache = new ConcurrentHashMap<>(); // onlyCode -> DeviceControl

    private static final ReentrantReadWriteLock RW = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.ReadLock  R  = RW.readLock();
    private static final ReentrantReadWriteLock.WriteLock W  = RW.writeLock();

    // ===================================== 刷新（全量覆盖） =====================================

    /**
     * 全量刷新：构建新快照（在锁外完成），然后写锁下“一次性替换”。
     * 刷新过程中对外读方法会阻塞，直到刷新完成，避免读到半成品。
     */
    public static void refresh(Collection<NetSite> sites) {
        // 1) 在锁外构建新快照，开销重的工作不阻塞读
        Map<String, NetSite> newMain = new ConcurrentHashMap<>();
        Map<String, Map<String, NetSite>> newBuckets = new ConcurrentHashMap<>();
        if (sites != null) {
            for (NetSite s : sites) {
                if (s == null || isBlank(s.getOnlyCode())) continue;
                // 主索引
                newMain.put(s.getOnlyCode(), s);
                // 分桶
                final String p = orEmpty(s.getPurpose());
                newBuckets.computeIfAbsent(p, k -> new ConcurrentHashMap<>())
                        .put(s.getOnlyCode(), s);
            }
        }

        // 2) 写锁独占：一次性切换引用
        W.lock();
        try {
            MAIN = newMain;
            BUCKETS = newBuckets;
            for(NetSite ns : BUCKETS.get(PURPOSE_WSC).values()){
                DeviceControl dc = deviceCache.putIfAbsent(ns.getOnlyCode(), new DeviceControl(ns));
                if(dc != null){
                    dc.setNetSite(ns);
                }
            }
        } finally {
            W.unlock();
        }
    }

    // ===================================== 单项写（Upsert/Remove） =====================================

    /** 新增或更新（按 onlyCode 覆盖）。 */
    public static void upsert(NetSite site) {
        if (site == null || isBlank(site.getOnlyCode())) return;
        W.lock();
        try {
            // 旧桶移除
            NetSite old = MAIN.put(site.getOnlyCode(), site);
            if (old != null) {
                String oldP = orEmpty(old.getPurpose());
                Map<String, NetSite> oldBucket = BUCKETS.get(oldP);
                if (oldBucket != null) oldBucket.remove(old.getOnlyCode());
            }
            // 新桶写入
            String p = orEmpty(site.getPurpose());
            BUCKETS.computeIfAbsent(p, k -> new ConcurrentHashMap<>())
                    .put(site.getOnlyCode(), site);
        } finally {
            W.unlock();
        }
    }

    /** 删除（按 onlyCode）。 */
    public static void remove(String onlyCode) {
        if (isBlank(onlyCode)) return;
        W.lock();
        try {
            NetSite old = MAIN.remove(onlyCode);
            if (old != null) {
                String p = orEmpty(old.getPurpose());
                Map<String, NetSite> bucket = BUCKETS.get(p);
                if (bucket != null) bucket.remove(onlyCode);
            }
        } finally {
            W.unlock();
        }
    }

    /** 批量 upsert（返回成功条数）。 */
    public static int upsertAll(Collection<NetSite> sites) {
        if (sites == null || sites.isEmpty()) return 0;
        int n = 0;
        W.lock();
        try {
            for (NetSite s : sites) {
                if (s == null || isBlank(s.getOnlyCode())) continue;
                NetSite old = MAIN.put(s.getOnlyCode(), s);
                if (old != null) {
                    String oldP = orEmpty(old.getPurpose());
                    Map<String, NetSite> oldBucket = BUCKETS.get(oldP);
                    if (oldBucket != null) oldBucket.remove(old.getOnlyCode());
                }
                String p = orEmpty(s.getPurpose());
                BUCKETS.computeIfAbsent(p, k -> new ConcurrentHashMap<>())
                        .put(s.getOnlyCode(), s);
                n++;
            }
        } finally {
            W.unlock();
        }
        return n;
    }

    /** 清空缓存。 */
    public static void clear() {
        W.lock();
        try {
            MAIN = new ConcurrentHashMap<>();
            BUCKETS = new ConcurrentHashMap<>();
        } finally {
            W.unlock();
        }
    }

    // ===================================== 读操作（读锁保护） =====================================

    /** 通用：按 onlyCode（忽略 purpose） */
    public static NetSite get(String onlyCode) {
        if (isBlank(onlyCode)) return null;
        R.lock();
        try {
            return MAIN.get(onlyCode);
        } finally {
            R.unlock();
        }
    }

    /** 按 purpose + onlyCode 精确获取 */
    public static NetSite get(String onlyCode, String purpose) {
        if (isBlank(onlyCode)) return null;
        R.lock();
        try {
            Map<String, NetSite> bucket = BUCKETS.get(orEmpty(purpose));
            return bucket == null ? null : bucket.get(onlyCode);
        } finally {
            R.unlock();
        }
    }

    /** 便捷：泡沫控制器 */
    public static NetSite pmByOnlyCode(String onlyCode) {
        return get(onlyCode, PURPOSE_PM);
    }

    /** 便捷：洗车机控制器 */
    public static NetSite wscByOnlyCode(String onlyCode) {
        return get(onlyCode, PURPOSE_WSC);
    }

    public static DeviceControl wscDeviceControl(String onlyCode){
        return deviceCache.get(onlyCode);
    }

    public static void refreshDeviceControl(String onlyCode, DeviceControl dc){
        deviceCache.put(onlyCode, dc);
    }


    /** 便捷：空压机控制器 */
    public static NetSite kyByOnlyCode(String onlyCode) {
        return get(onlyCode, PURPOSE_KY);
    }

    /** 快照：全部 */
    public static List<NetSite> snapshotAll() {
        R.lock();
        try {
            return Collections.unmodifiableList(new ArrayList<>(MAIN.values()));
        } finally {
            R.unlock();
        }
    }


    public static List<NetSite> wscSnapshotNetSite(){
        return snapshotByPurpose(PURPOSE_WSC);
    }

    /** 快照：按 purpose */
    public static List<NetSite> snapshotByPurpose(String purpose) {
        R.lock();
        try {
            Map<String, NetSite> bucket = BUCKETS.get(orEmpty(purpose));
            if (bucket == null) return Collections.emptyList();
            return Collections.unmodifiableList(new ArrayList<>(bucket.values()));
        } finally {
            R.unlock();
        }
    }

    /** 统计：各桶数量 */
    public static Map<String, Integer> stats() {
        R.lock();
        try {
            Map<String, Integer> m = new LinkedHashMap<>();
            m.put("ALL", MAIN.size());
            m.put(PURPOSE_PM,  sizeOfBucket(PURPOSE_PM));
            m.put(PURPOSE_WSC, sizeOfBucket(PURPOSE_WSC));
            m.put(PURPOSE_KY,  sizeOfBucket(PURPOSE_KY));
            return m;
        } finally {
            R.unlock();
        }
    }

    /** 分组只读视图 */
    public static Map<String, List<NetSite>> groupedSnapshot() {
        R.lock();
        try {
            return Collections.unmodifiableMap(
                    MAIN.values().stream().collect(Collectors.groupingBy(
                            ns -> orEmpty(ns.getPurpose()),
                            Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList)
                    ))
            );
        } finally {
            R.unlock();
        }
    }

    private static int sizeOfBucket(String purpose) {
        Map<String, NetSite> b = BUCKETS.get(orEmpty(purpose));
        return b == null ? 0 : b.size();
    }

    // ===================================== 辅助 =====================================

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String orEmpty(String s) {
        return s == null ? "" : s;
    }
}
