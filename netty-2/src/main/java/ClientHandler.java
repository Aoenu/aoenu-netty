import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * This is Description
 *
 * @author wubaoben
 * @date 2018/12/21
 */
public class ClientHandler extends ChannelHandlerAdapter {
    private final ByteBuf firstMessage;

    public ClientHandler() {
        byte[] req = "天王盖地虎".getBytes();
        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(firstMessage);//推送消息
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req, "UTF-8");
        System.out.println("Client 接收到的消息 :" + body);
        //ctx.close();//接受完消息关闭连接，注释掉可以看到释放资源，否则请求完后就关闭连接是看不到异常情况的
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("释放资源:" + cause.getMessage());//不重写将会看到堆栈信息以及资源无法关闭
        ctx.close();
    }
}
