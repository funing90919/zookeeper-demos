package top.osfun;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ABC三个线程，A打印AA，B打印BB，C打印CC，循环10次
 * Created by Jacky on 2019-05-06 22:40.
 */
public class ABCPrint {
    private int threadNo = 1;
    private Lock lock = new ReentrantLock();
    private Condition c1 = lock.newCondition();
    private Condition c2 = lock.newCondition();
    private Condition c3 = lock.newCondition();

    public void print5() throws InterruptedException {
        lock.lock();
        while (threadNo != 1) {
            c1.await();
        }
        System.out.print("AA");
        threadNo = 2;
        c2.signal();
        lock.unlock();
    }

    public void print10() throws InterruptedException {
        lock.lock();
        while (threadNo != 2) {
            c2.await();
        }
        System.out.print("BB");
        threadNo = 3;
        c3.signal();
        lock.unlock();
    }

    public void print15() throws InterruptedException {
        lock.lock();
        while (threadNo != 3) {
            c3.await();
        }
        System.out.print("CC");
        threadNo = 1;
        c1.signal();
        lock.unlock();
    }

    public static void main(String[] args) {
        ABCPrint abcPrint = new ABCPrint();
        new Thread(() -> {
            for (int i = 1; i <= 10; i++)
                try {
                    abcPrint.print5();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }, "A").start();
        new Thread(() -> {
            for (int i = 1; i <= 10; i++)
                try {
                    abcPrint.print10();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }, "B").start();
        new Thread(() -> {
            for (int i = 1; i <= 10; i++)
                try {
                    abcPrint.print15();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }, "C").start();
    }
}
