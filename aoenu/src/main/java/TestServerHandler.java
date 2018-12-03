import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * This is Description
 * 多路复用类
 * 它是一个独立的线程，负责轮询多路复器Selector,可以处理多个客户端的并发接入
 *
 * @author wubaoben
 * @date 2018/12/03
 */
public class TestServerHandler implements Runnable {

    private Selector selector; //多路复用器
    private ServerSocketChannel serverSocketChannel;

    public TestServerHandler(int port) {
        try {
            selector = Selector.open(); //打开多路复用器
            serverSocketChannel = ServerSocketChannel.open(); //打开ServerSocket通道
            serverSocketChannel.configureBlocking(false); //设置异步非阻塞模式,与Selector使用 Channel 必须处于非阻塞模式
            serverSocketChannel.socket().bind(new InetSocketAddress(port), 1024); //绑定端口为4040并且初始化系统资源位1024个
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT); //将Channel管道注册到Selector中去,监听OP_ACCEPT操作
            System.out.println("TestServer启动成功,当前监听的端口 : " + port);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1); //如果初始化失败，退出
        }
    }

    public void run() {
        while (true) {
            try {
                //设置selector的休眠时间为1s，无论是否有读写等事件发生，selector每隔1s都被唤醒一次。
                selector.select(1000);
                //当有处于就绪状态的Channel时，selector就返回就绪状态的Channel的SelectionKey集合。
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectedKeys.iterator();
                SelectionKey key = null;
                //通过对就绪状态的Channel集合进行迭代，可以进行网络的异步读写操作。
                while (it.hasNext()) {
                    key = it.next();
                    it.remove(); //删掉处理过的key
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
            }
        }
    }

    private void handleInput(SelectionKey key) throws Exception {
        if (key.isValid()) {//处理新接入的请求消息
            if (key.isAcceptable()) {
                SocketChannel accept = ((ServerSocketChannel) key.channel()).accept();
                accept.configureBlocking(false);
                //添加新的连接到selector中
                accept.register(this.selector, SelectionKey.OP_READ);
            }
            if (key.isReadable()) {//读取数据
                SocketChannel sc = (SocketChannel) key.channel();
                //由于实现我们得知客户端发送的码流大小，作为例程，我们开辟一个1K的缓冲区
                ByteBuffer buffer = ByteBuffer.allocate(1024);//一次最多读取1024
                //由于已经设置SocketChannel为异步非阻塞模式，因此它的read是非阻塞的。
                int read = sc.read(buffer);
                /*
                 * read>0  读到了字节，对字节进行编解码；
                 * read=0  没有读取到字节，属于正常场景，忽略；
                 * read=-1 链路已经关闭，需要关闭SocketChannel，释放资源
                 */
                if (read > 0) {
                    //反转缓冲区 将缓冲区当前的limit设置为position，position设置为0，用于后续对缓冲区的读取操作。
                    buffer.flip();
                    //根据缓冲区可读的字节个数创建字节数组
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    String msg = new String(bytes, "UTF-8");
                    System.out.println("TimeServer 接收到的消息 :" + msg);
                    doWrite(sc, "小鸡炖蘑菇...");
                } else if (read < 0) {
                    //对端链路关闭
                    key.cancel();
                    sc.close();
                } else {
                    //读取0个字节忽略
                }
            }
        }
    }

    private void doWrite(SocketChannel channel, String resp) throws IOException {
        if (resp != null && resp.trim().length() > 0) {
            byte[] bytes = resp.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);//根据字节大小创建一个Buffer
            writeBuffer.put(bytes);//调用ByteBuffer的put操作将字节数组复制到缓冲区
            writeBuffer.flip();//反转缓冲区
            channel.write(writeBuffer);//调用管道API将数据写出
        }
    }
}
