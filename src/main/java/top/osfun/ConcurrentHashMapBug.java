package top.osfun;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConcurrentHashMap递归创建时BUG
 * 死循环
 * Created by Jacky on 2019-05-06 21:17.
 */
public class ConcurrentHashMapBug {

    private volatile Map<Integer, Integer> cache = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConcurrentHashMapBug demo = new ConcurrentHashMapBug();
        demo.fibonaacci(80);
    }

    public int fibonaacci(Integer i) {
        if (i == 0 || i == 1)
            return i;
        return cache.computeIfAbsent(i, key -> {
            System.out.println("fibonaacci" + key);
            return fibonaacci(key - 1) + fibonaacci(key - 2);
        });
    }

}
