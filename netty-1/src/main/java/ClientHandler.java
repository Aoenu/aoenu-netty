import org.jboss.netty.channel.*;

import java.util.Scanner;

/**
 * This is Description
 *
 * @author wubaoben
 * @date 2018/12/18
 */
public class ClientHandler extends SimpleChannelHandler {
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        System.out.println("messageReceived!");
        //打印接收到的消息
        System.out.println(e.getMessage());
        super.messageReceived(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        System.out.println("exceptionCaught!");
        super.exceptionCaught(ctx, e);
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("channelConnected!");
        Channel channel = ctx.getChannel();
        Scanner scanner = new Scanner(System.in);
//        while (true) {
        System.out.println("请输入：");
        channel.write(scanner.next());
//        }
        super.channelConnected(ctx, e);
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("channelDisconnected!");
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        System.out.println("channelClosed!");
        super.channelClosed(ctx, e);
    }
}
