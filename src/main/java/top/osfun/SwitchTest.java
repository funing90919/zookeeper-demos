package top.osfun;

/**
 * Created by Jacky on 2019-05-09 20:09
 */
public class SwitchTest {
    public static void main(String[] args) throws InterruptedException {
        m(2);
    }

    public static void m(int a) {
        int result = 1;
        switch (a) {
            case 1:
                result = result + a;
            case 2:
                result = result + a;
            case 3:
                result = result + a;
                break;
            case 4:
                result = result + a;
            default:
                result = result + a;
        }
        System.out.print(result);
    }
}