import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is Description
 *
 * @author wubaoben
 * @date 2018/12/18
 */
public class NettyClient {
    public static void main(String[] args) {
        //创建NIO客户端启动辅助类
        ClientBootstrap clientBootstrap = new ClientBootstrap();
        //创建线程组
        ExecutorService boss = Executors.newCachedThreadPool();
        ExecutorService worker = Executors.newCachedThreadPool();
        //设置客户端nioSocket工厂
        clientBootstrap.setFactory(new NioClientSocketChannelFactory(boss, worker));
        //设置管道工厂
        clientBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipeline = Channels.pipeline();
                //编码解码
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast("decoder", new StringDecoder());
                //自定义handler
                pipeline.addLast("ClientHandler", new ClientHandler());
                return pipeline;
            }
        });
        //绑定启动IP地址端口号
        clientBootstrap.connect(new InetSocketAddress("127.0.0.1", 6666));
        System.out.println("Client Start!");
    }
}
