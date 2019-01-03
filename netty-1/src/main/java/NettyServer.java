

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is Description
 *
 * @author wubaoben
 * @date 2018/12/18
 */
public class NettyServer {
    public static void main(String[] args) {

        //创建NIO服务端启动辅助类
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        //创建线程组
        ExecutorService boss = Executors.newCachedThreadPool();
        ExecutorService worker = Executors.newCachedThreadPool();
        //设置服务端nioSocket 工厂
        serverBootstrap.setFactory(new NioServerSocketChannelFactory(boss, worker));
        //设置管道工厂
        serverBootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() throws Exception {
                //获取管道
                ChannelPipeline pipeline = Channels.pipeline();
                //添加编码器解码器，如不加 handler的messageReceived方法里需手动将ChannelBiffer装换为string类型
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                //服务器自定义handle
                pipeline.addLast("HelloHandler", new ServerHandler());
                return pipeline;
            }
        });
        //绑定端口号
        serverBootstrap.bind(new InetSocketAddress(6666));
        System.out.println("Server Start!");
    }
}
