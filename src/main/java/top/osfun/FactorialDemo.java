package top.osfun;

/**
 * 编写一个Java程序在屏幕上输出1！+2！+3！+……+10！的和
 * Created by Jacky on 2019-05-09 21:18.
 */
public class FactorialDemo {
    public static void main(String[] args) {
        int i, j, temp, sum = 0;
        for (i = 1; i <= 10; i++) {
            temp = 1;
            for (j = 1; j <= i; j++) {
                temp = temp * j;
            }
            sum = sum + temp;
        }
        System.out.println(sum);
    }

    /**
     * 古典问题：有一对兔子，从出生后第3个月起每个月都生一对兔子，小兔子长到第三个月后每个月又生一对兔子。
     * 假如兔子都不死，问每个月的兔子总数为多少？
     */
    public static void main1() {
        System.out.println("第1个月的兔子对数:    1");
        System.out.println("第2个月的兔子对数:    1");
        int f1 = 1, f2 = 1, f, M = 10;
        for (int i = 3; i <= M; i++) {
            f = f2;
            f2 = f1 + f2;
            f1 = f;
            System.out.println("第" + i + "个月的兔子对数: " + f2);
        }
    }
}
