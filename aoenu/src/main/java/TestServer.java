/**
 * This is Description
 *
 * @author wubaoben
 * @date 2018/12/03
 */
public class TestServer {
    public static void main(String[] args) {
        int port = 6666;
        TestServerHandler testServerHandler = new TestServerHandler(port);
        new Thread(testServerHandler,"TestServer-NIO").start();
    }
}
