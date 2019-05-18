package top.osfun.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class DistributeLock implements Watcher {

    private ZooKeeper zk = null;
    private String rootLockNode;            // 锁的根节点
    private String lockName;                // 竞争资源，用来生成子节点名称
    private String currentLock;             // 当前锁
    private String waitLock;                // 等待的锁（前一个锁）
    private CountDownLatch countDownLatch;  // 计数器（用来在加锁失败时阻塞加锁线程）
    private int sessionTimeout = 30000;     // 超时时间

    // 1. 构造器中创建ZK链接，创建锁的根节点
    public DistributeLock(String zkAddress, String rootLockNode, String lockName) {
        this.rootLockNode = rootLockNode;
        this.lockName = lockName;
        try {
            // 创建连接，zkAddress格式为：IP:PORT
            zk = new ZooKeeper(zkAddress, sessionTimeout, this);
            // 检测锁的根节点是否存在，不存在则创建
            Stat stat = zk.exists(rootLockNode, false);
            if (null == stat) {
                zk.create(rootLockNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }

    // 2. 加锁方法，先尝试加锁，不能加锁则等待上一个锁的释放
    public boolean lock() {
        if (tryLock()) {
            System.out.println("线程【" + Thread.currentThread().getName() + "】加锁（" + currentLock + "）成功！");
            return true;
        } else {
            return waitOtherLock(waitLock, sessionTimeout);
        }
    }

    private boolean tryLock() {
        // 分隔符，便于区分路径名称和编号：/locks/test_lock_000000002
        String split = "_lock_";
        if (lockName.contains("_lock_")) {
            throw new RuntimeException("lockName can't contains '_lock_' ");
        }
        try {
            // 创建锁节点（临时有序节点）
            currentLock = zk.create(rootLockNode + "/" + lockName + split, new byte[0],
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            System.out.println("线程【" + Thread.currentThread().getName() + "】创建锁节点（" + currentLock + "）成功，开始竞争...");
            // 取所有子节点，即需要获取锁的所有节点[test_lock_000000002,test_lock_000000003...]
            List<String> nodes = zk.getChildren(rootLockNode, false);
            // 取所有竞争lockName的锁
            List<String> lockNodes = new ArrayList<>();
            for (String nodeName : nodes) {
                // 节点与test1(锁)相等的节点才需要锁
                if (nodeName.split(split)[0].equals(lockName)) {
                    lockNodes.add(nodeName);
                }
            }
            Collections.sort(lockNodes);
            // 取最小节点与当前锁节点比对加锁
            String currentLockPath = rootLockNode + "/" + lockNodes.get(0);
            if (currentLock.equals(currentLockPath)) {
                return true;
            }
            // 加锁失败，设置前一节点为等待锁节点:test_lock_000000002
            String currentLockNode = currentLock.substring(currentLock.lastIndexOf("/") + 1);
            int preNodeIndex = Collections.binarySearch(lockNodes, currentLockNode) - 1;
            waitLock = lockNodes.get(preNodeIndex);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean waitOtherLock(String waitLock, int sessionTimeout) {
        boolean islock = false;
        try {
            // 监听等待锁节点
            String waitLockNode = rootLockNode + "/" + waitLock;
            Stat stat = zk.exists(waitLockNode, true);
            if (null != stat) {
                System.out.println("线程【" + Thread.currentThread().getName() + "】锁（" + currentLock + "）加锁失败，等待锁（" + waitLockNode + "）释放...");
                // 设置计数器，使用计数器阻塞线程
                countDownLatch = new CountDownLatch(1);
                islock = countDownLatch.await(sessionTimeout, TimeUnit.MILLISECONDS);
                countDownLatch = null;
                if (islock) {
                    System.out.println("线程【" + Thread.currentThread().getName() + "】锁（" + currentLock + "）加锁成功，锁（" + waitLockNode + "）已经释放");
                } else {
                    System.out.println("线程【" + Thread.currentThread().getName() + "】锁（" + currentLock + "）加锁失败...");
                }
            } else {
                islock = true;
            }
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return islock;
    }

    // 3. 释放锁
    public void unlock() throws InterruptedException {
        try {
            Stat stat = zk.exists(currentLock, false);
            if (null != stat) {
                System.out.println("线程【" + Thread.currentThread().getName() + "】释放锁 " + currentLock);
                zk.delete(currentLock, -1);
                currentLock = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        } finally {
            zk.close();
        }
    }

    // 4. 监听器回调
    @Override
    public void process(WatchedEvent watchedEvent) {
        if (null != countDownLatch && watchedEvent.getType() == Event.EventType.NodeDeleted) {
            // 计数器减一，恢复线程操作
            countDownLatch.countDown();
        }
    }

    public static void main(String[] args) {
        Runnable runnable = () -> {
            DistributeLock lock = new DistributeLock("120.79.28.176:2181,120.79.28.176:2182,120.79.28.176:2183", "/locks", "test");
            if (lock.lock()) {
                doSomething();
                try {
                    Thread.sleep(1000);
                    lock.unlock();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }

    public static void doSomething() {
        System.out.println("线程【" + Thread.currentThread().getName() + "】正在运行...");
    }
}