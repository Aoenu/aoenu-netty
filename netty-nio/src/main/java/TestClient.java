/**
 * This is Description
 *
 * @author wubaoben
 * @date 2018/12/03
 */
public class TestClient {
    public static void main(String[] args) {
        int port = 6666;
        TestClientHandler testClientHandler = new TestClientHandler("127.0.0.1",port);
        new Thread(testClientHandler, "TestClient-NIO").start();
    }
}
