package serverNIO;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;


/**
 * @Description NIO实现多人聊天室的服务器端
 * @Author Pengnan
 * @CreateTime 2021年04月12日 20:21:00
 */
public class ChatServer {
    private final static int DEFAULT_PORT=9999;
    private final String QUIT="quit";
    private static final int BUFFER=1024;

    private ServerSocketChannel server;
    private Selector selector;
    private ByteBuffer rBuffer=ByteBuffer.allocate(BUFFER);//客户端——>服务器
    private ByteBuffer wBuffer=ByteBuffer.allocate(BUFFER);//服务器（转发）——>其它客户端
    private Charset charset= Charset.forName("UTF-8");
    private int port;//用户自定义端口

    public ChatServer(){
        this(DEFAULT_PORT);
    }
    public ChatServer(int port){
        this.port=port;
    }

    public void start(){
        try {
            server=ServerSocketChannel.open();
            server.configureBlocking(false);//设定为非阻塞模式！！！
            server.socket().bind(new InetSocketAddress(port));
            //开启Selector
            selector=Selector.open();
            //注册ServerSocket的ACCEPT事件
            server.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口："+port+"...");

            //监听事件，并处理
            while(true){
                selector.select();//这个函数是阻塞式的，除非有注册的事件触发了，才会返回，返回值是触发事件通道的个数
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for(SelectionKey key:selectionKeys){
                    handles(key);//处理对应的触发事件
                }
                selectionKeys.clear();//清空这次的触发事件，不影响下次触发！！！
            }
        } catch (IOException exception) {
            System.out.println(System.currentTimeMillis()+"IOEXCEPTION");
            exception.printStackTrace();
        }finally {
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        //ACCEPT事件：和客户端建立连接
        if(key.isAcceptable()){
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();//客户端的SocketChannel需要通过服务器端的ServerSocketChannel的accept()获取，表示建立连接
            if(client!=null){
                client.configureBlocking(false);
                client.register(selector,SelectionKey.OP_READ);
                System.out.println(getClientName(client)+"已连接");
            }
        }
        //READ事件：客户端发来消息
        else if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            String fwdMsg=receive(client);
            if(fwdMsg.isEmpty()){//等价于length（）=0
                key.cancel();
                selector.wakeup();
            }else{
                //正常
                System.out.println(getClientName(client)+":"+fwdMsg);
                forwardMessage(client,fwdMsg);
                //检查客户端否要退出
                if(isToQuit(fwdMsg)){
                    key.cancel();
                    selector.wakeup();
                    System.out.println(getClientName(client)+"已断开");
                }
            }
        }
    }

    //服务器转发消息给其它客户端
    private void forwardMessage(SocketChannel client, String fwdMsg) throws IOException {
        for(SelectionKey key:selector.keys()){
            Channel connectClient=key.channel();
            if(connectClient instanceof ServerSocketChannel){
                continue;
            }
            if(key.isValid()&&!client.equals(connectClient)){
                wBuffer.clear();
                //把来自客户端的消息放入wBuffer
                wBuffer.put(charset.encode(getClientName(client)+":"+fwdMsg));
                wBuffer.flip();//写模式切换成读模式
                while (wBuffer.hasRemaining()){
                    ((SocketChannel)connectClient).write(wBuffer);//写入其它通道
                }
            }
        }
    }
    //服务器收到来自客户端的消息  有什么用rBuffer？？:读取来自客户端的消息
    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while(client.read(rBuffer)>0);//rBuffer处于写模式
        rBuffer.flip();//写模式切换到读模式


        return String.valueOf(charset.decode(rBuffer));
    }

    public String getClientName(SocketChannel client){
        return "客户端["+client.socket().getPort()+"]";
    }

    public boolean isToQuit(String msg){
        return QUIT.equals(msg);
    }
    public void close(Closeable closeable){
        if(closeable!=null){
            try {
                closeable.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }

}
