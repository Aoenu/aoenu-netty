import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * This is Description
 * <p>
 * 处理异步连接和读写操作
 *
 * @author wubaoben
 * @date 2018/12/03
 */
public class TestClientHandler implements Runnable {

    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;


    public TestClientHandler(String host, int port) {
        this.host = host == null ? "127.0.0.1" : host;
        this.port = port;
        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            //设置为异步非阻塞模式，同时还可以设置SocketChannel的TCP参数。例如接收和发送的TCP缓冲区大小
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);//异常情况断开连接
        }
    }

    public void run() {
        try {
            doConnect();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);//异常情况断开连接
        }
        while (!stop) {
            try {
                selector.select(1000);
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                SelectionKey key = null;
                while (it.hasNext()) {//轮询多路复用器Selector，当有就绪的Channel时
                    key = it.next();
                    it.remove();
                    try {
                        handleInput(key);
                    } catch (Exception e) {
                        if (key != null) {
                            key.cancel();
                            if (key.channel() != null) {
                                key.channel().close();
                            }
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

//        //多路复用器关闭后，所有注册在上面的Channel和Pipe等资源都会被自动注册并关闭，所以不需要重复释放资源。
//        /*
//         * 由于多路复用器上可能注册成千上万的Channel或者pipe，如果一一对这些资源进行释放显然不合适。
//         * 因此，JDK底层会自动释放所有跟此多路复用器关联的资源。
//         */
//
//        if(selector!=null){
//            try {
//                selector.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

    }

    private void handleInput(SelectionKey key) throws IOException {
        //验证这个key是否有效
        if (key.isValid()) {
            //判断是否连接成功
            SocketChannel sc = (SocketChannel) key.channel();
            if (key.isConnectable()) {//处于连接状态，说明服务器已经返回ACK应答消息
                if (sc.finishConnect()) {//对连接结果进行判断
                    /*
                     * 将SocketChannel注册到多路复用器上，注册SelectionKey.OP_READ操作位，
                     * 监听网络读操作，然后发送请求消息给服务端。
                     */
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                } else {
                    System.exit(1);//连接失败，进程退出
                }
            }
            if (key.isReadable()) {
                //开辟缓冲区 创建读取所需Buffer
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                //异步读取
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    readBuffer.flip();
                    byte[] bytes = new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body = new String(bytes, "UTF-8");
                    System.out.println("TimeClient 接收到的消息: " + body);
                    //如果接收完毕退出循环
                    this.stop = true;
                } else if (readBytes < 0) {
                    //对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    //读到0字节，忽略
                }
            }
        }
    }

    private void doConnect() throws IOException {
        //如果直接连接成功，则将SocketChannel注册到多路复用器Selector上，发送请求消息，读应答
        if (socketChannel.connect(new InetSocketAddress(host, port))) {
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        } else {
            /*
             * 如果没有直接连接成功，则说明服务端没有返回TCP握手应答信息，但这并不代表连接失败，
             * 我们需要将SocketChannel注册到多路复用器Selector上，注册SelectionKey.OP_CONNECT，
             * 当服务端返回TCP syn-ack消息后，Selector就能轮询到整个SocketChannel处于连接就绪状态。
             */
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }
    }

    private void doWrite(SocketChannel socketChannel) throws IOException {
        byte[] req = "天王盖地虎".getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(req.length);
        buffer.put(req);//将字节数组复制到缓冲区
        buffer.flip();//反转缓冲区
        socketChannel.write(buffer); //发送是异步的，所以会存在"半包写"问题
        if (!buffer.hasRemaining()) {//如果缓冲区中的消息全部发送完成
            System.out.println("消息发送成功");
        }
    }
}
