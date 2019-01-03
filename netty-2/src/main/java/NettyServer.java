import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * This is Description
 *
 * @author wubaoben
 * @date 2018/12/21
 */
public class NettyServer {

    public static void bind(Integer port) {
        EventLoopGroup masterGroup = new NioEventLoopGroup();//一个是用于处理服务端接受客户端连接的
        EventLoopGroup workerGroup = new NioEventLoopGroup();//一个是进行网路通信的（网络读写）
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();//创建NIO服务端启动辅助类 用于服务器通道的一系列配置
            bootstrap.group(masterGroup, workerGroup)   //绑定两个线程组
                    .channel(NioServerSocketChannel.class) //指定NIO模式
                    .option(ChannelOption.SO_BACKLOG, 1024)//连接数
                    .option(ChannelOption.TCP_NODELAY, true)//不延迟，立即推送
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new ServerHandler());
                        }
                    });
            //绑定端口，同步等待成功,
            System.out.println("绑定端口,同步等待成功......");
            ChannelFuture future = bootstrap.bind(port).sync();
            //等待服务端监听端口关闭
            future.channel().closeFuture().sync();
            System.out.println("等待服务端监听端口关闭......");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //优雅退出释放线程池
            masterGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            System.out.println("优雅退出释放线程池......");
        }
    }

    public static void main(String[] args) {
        NettyServer.bind(6666);
    }
}
