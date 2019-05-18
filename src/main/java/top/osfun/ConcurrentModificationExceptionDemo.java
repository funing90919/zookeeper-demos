package top.osfun;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jacky on 2019-05-06 21:55
 *
 [null, null, AA2]
 Exception in thread "8" java.util.ConcurrentModificationException
 [null, null, AA2, AA4, AA5]
 at java.util.ArrayList$Itr.checkForComodification(ArrayList.java:901)
 [null, null, AA2, AA4]
 at java.util.ArrayList$Itr.next(ArrayList.java:851)
 at java.util.AbstractCollection.toString(AbstractCollection.java:461)
 */
public class ConcurrentModificationExceptionDemo {

    public static void main(String[] args) throws InterruptedException {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <=10; i++) {
            final int a = i;
            new Thread(()->{
                list.add("AA" + a);
                System.out.println(list);
            }, String.valueOf(i)).start();
        }
    }

}
