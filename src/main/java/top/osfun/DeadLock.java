package top.osfun;

/**
 * Created by Jacky on 2019-05-06 22:12.
 */
public class DeadLock implements Runnable {
    private String lockA;
    private String lockB;

    public DeadLock(String a, String b) {
        this.lockA = a;
        this.lockB = b;
    }

    @Override
    public void run() {
        synchronized (lockA) {
            System.out.println(Thread.currentThread().getName() + "获得A，等待B");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lockB) {
                System.out.println(Thread.currentThread().getName() + "获得B，等待A");
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        String lockA = "lockA";
        String lockB = "lockB";
        new Thread(new DeadLock(lockA,lockB)).start();
        new Thread(new DeadLock(lockB,lockA)).start();
    }
}
