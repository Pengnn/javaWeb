package Test;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description AIO 回音壁服务器端实现
 * @Author Pengnan
 * @CreateTime 2021年04月13日 14:46:00
 */
public class ServerAIO {
    private final String LOCALHOST="localhost";
    private final int DEFAULT_PORT=8888;
    AsynchronousServerSocketChannel serverChannel;

    private void close(Closeable closeable){
        if(closeable!=null){
            try {
                closeable.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            //AsyncChannelGroup：可以被多个异步通道共享的资源群组：线程池
            serverChannel=AsynchronousServerSocketChannel.open();
            serverChannel.bind(new InetSocketAddress(LOCALHOST,DEFAULT_PORT));
            System.out.println("启动服务器，监听端口"+DEFAULT_PORT+"...");

            serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {//①
                @Override
                public void completed(AsynchronousSocketChannel clientChannel, Object attachment) {
                    if(serverChannel.isOpen()){
                        serverChannel.accept(null,this);
                    }
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    Map<String,Object> info=new HashMap<>();
                    info.put("type","read");
                    info.put("buffer",buffer);
                    ClientHandler handler=new ClientHandler(clientChannel);
                    clientChannel.read(buffer, info,handler);//②
                }
                @Override
                public void failed(Throwable exc, Object attachment) {
                }
            });

            while(true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }finally {
            close(serverChannel);
        }
    }

    private class ClientHandler implements CompletionHandler<Integer,Object>{
        private AsynchronousSocketChannel clientChannel;
        public ClientHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel=clientChannel;
        }
        @Override
        public void completed(Integer result, Object attachment) {
            Map<String,Object> info=(Map<String, Object>) attachment;
            String type=(String) info.get("type");
            if("read".equals(type)){
                //借助Buffer，可读事件需要把数据写入客户端的通道中
                ByteBuffer buffer = (ByteBuffer) info.get("buffer");
                buffer.flip();
                //下一步执行"write"事件
                info.put("type","write");
                clientChannel.write(buffer,info,this);//③
                buffer.clear();
            }else if("write".equals(type)){
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                info.put("type","read");
                info.put("buffer",buffer);
                //下一步执行"read"事件
                clientChannel.read(buffer,info,this);//④
            }
        }
        @Override
        public void failed(Throwable exc, Object attachment) {
        }
    }

    public static void main(String[] args) {
        final ServerAIO serverAIO = new ServerAIO();
        serverAIO.start();
    }
}
