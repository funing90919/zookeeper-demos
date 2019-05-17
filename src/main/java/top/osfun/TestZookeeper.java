package top.osfun;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by Jacky on 2019-04-30 11:32
 */
public class TestZookeeper {

    private String connectString = "120.79.28.176:2182,120.79.28.176:2183";

    private int sessionTimeout = 2000;

    private ZooKeeper zkClient;

    @Before
    public void init() throws IOException {
        zkClient = new ZooKeeper(connectString, sessionTimeout, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                List<String> children = null;
                try {
                    children = zkClient.getChildren("/", true);
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                children.forEach((e) -> {
                    System.out.println(e);
                });
            }
        });
    }

    // 1.创建节点
    @Test
    public void create() throws KeeperException, InterruptedException {
        String path = zkClient.create("/china", "hubeisheng".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(path);
    }

    // 2.获取子节点并监控数据变化
    @Test
    public void getDataAndWatch() throws KeeperException, InterruptedException {
        List<String> children = zkClient.getChildren("/", true);
        children.forEach((e) -> {
            System.out.println(e);
        });
    }

   // 3.判断节点是否存在
    @Test
    public void exist() throws KeeperException, InterruptedException {
        Stat exists = zkClient.exists("/china", false);
        System.out.print(exists == null);
    }

}
