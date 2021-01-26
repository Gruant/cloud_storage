import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;



public class ServerHandler extends ChannelInboundHandlerAdapter {

    private File file = new File("./serverDir/out.txt");
    private FileOutputStream fos = new FileOutputStream(file);

    private static final Logger LOG = LoggerFactory.getLogger(ServerHandler.class);

    public ServerHandler() throws FileNotFoundException {
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fos = new FileOutputStream(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("Client accepted");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOG.info("Client disconnected");
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        try {
            if (buf.isReadable()) {
                buf.readBytes(fos, buf.readableBytes());
                fos.flush();
            } else {
                System.out.println("I want to close fileoutputstream!");
                buf.release();
                fos.flush();
                fos.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();

        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage() + '\n').addListener(ChannelFutureListener.CLOSE);
        }
    }


    //читаем содержимое файла
//    @Override
//    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
//        RandomAccessFile raf = null;
//        long length = -1;
//        try {
//            raf = new RandomAccessFile(s, "r");
//            length = raf.length();
//        } catch (Exception e) {
//            channelHandlerContext.writeAndFlush("ERR: " + e.getClass().getSimpleName() + ": " + e.getMessage() + '\n');
//            return;
//        } finally {
//            if (length < 0 && raf != null) {
//                raf.close();
//            }
//        }
//
//        channelHandlerContext.write("OK: " + raf.length() + '\n');
//        channelHandlerContext.write(new ChunkedFile(raf));
//        channelHandlerContext.writeAndFlush("\n");
//    }
}
