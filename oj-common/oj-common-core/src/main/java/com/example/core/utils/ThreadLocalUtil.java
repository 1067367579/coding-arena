package com.example.core.utils;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadLocalUtil {
    private static final TransmittableThreadLocal<Map<String,Object>> THREAD_LOCAL =
            new TransmittableThreadLocal<>();

    //赋值
    public static void set(String key, Object value) {
        Map<String, Object> map = getLocalMap();
        map.put(key,value == null ? "" : value);
    }

    //获取出来
    public static Map<String,Object> getLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    //请求处理完要删掉 不然会造成运行时内存泄漏
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
