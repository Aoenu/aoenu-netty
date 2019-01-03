import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * This is Description
 *
 * @author wubaoben
 * @date 2018/12/21
 */
public class ServerHandler extends ChannelHandlerAdapter {

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        //发生异常时候，执行重写后的 exceptionCaught 进行资源关闭
        ctx.close();
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead");
        //channelRead方法中的msg参数（服务器接收到的客户端发送的消息）强制转换成ByteBuf类型
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");//返回String，指定编码(有可能出现不支持的编码类型异常，所以要trycatch
        System.out.println("Server 接收到的消息 :" + body);
        ByteBuf resp = Unpooled.copiedBuffer("小鸡炖蘑菇...".getBytes());
        ctx.write(resp);// 将消息放入缓冲数组
        super.channelRead(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
        //将消息队列中信息写入到SocketChannel中去,解决了频繁唤醒Selector所带来不必要的性能开销
        //Netty的 write 只是将消息放入缓冲数组,再通过调用 flush 才会把缓冲区的数据写入到 SocketChannel
        ctx.flush();
        super.channelReadComplete(ctx);
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        System.out.println("connect");
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("disconnect");
        super.disconnect(ctx, promise);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        System.out.println("close");
        super.close(ctx, promise);
    }
}
