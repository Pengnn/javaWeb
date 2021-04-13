package clientNIO;


import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * @Description NIO实现多人聊天客户端
 * @Author Pengnan
 * @CreateTime 2021年04月12日 21:39:00
 */
public class ChatClient {
    private final static String DEFAULT_SERVER_HOST="127.0.0.1";
    private final static int DEFAULT_SERVER_PORT=9999;
    private final String QUIT="quit";
    private final int BUFFER=1024;

    private String host;
    private int port;
    private SocketChannel client;
    private ByteBuffer rBuffer=ByteBuffer.allocate(BUFFER);
    private ByteBuffer wBuffer=ByteBuffer.allocate(BUFFER);
    private Charset charset= Charset.forName("UTF-8");
    private Selector selector;
    public ChatClient(){
        this(DEFAULT_SERVER_HOST,DEFAULT_SERVER_PORT);
    }
    public ChatClient(String host,int port){
        this.host=host;
        this.port=port;
    }
    public void start(){
       try {
            client = SocketChannel.open();
            client.configureBlocking(false);
            client.connect(new InetSocketAddress(host,port));

            selector = Selector.open();
            client.register(selector, SelectionKey.OP_CONNECT);
            while(true){
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for(SelectionKey key:selectionKeys){
                    handles(key);
                }
                selectionKeys.clear();
            }
        } catch (ClosedChannelException e) {
           e.printStackTrace();
       } catch (IOException exception) {
           exception.printStackTrace();
       }catch (ClosedSelectorException e){
           //因为selector在send()函数判断quit时已经关闭了，所以会捕捉到这个异常，不需要进行处理
        }finally {
           System.out.println("close");
            close(selector);
        }
    }

    private void handles(SelectionKey key) throws IOException {
        //CONENCT事件：连接就绪事件
        if(key.isConnectable()){
            SocketChannel client = (SocketChannel) key.channel();
            if(client.isConnectionPending()){
                client.finishConnect();
                //连接建立，新建线程处理用户输入
                new Thread(new UserInputHandler(this)).start();
            }
            client.register(selector,SelectionKey.OP_READ);
        }
        //READ事件：读取来自服务器的消息
        else if(key.isReadable()){
            SocketChannel client = (SocketChannel) key.channel();
            if(client.isConnected()){
                String msg=receive(client);
                System.out.println(msg);
//                if(msg.isEmpty()){  //这个条件不会发生，因为消息经过服务器转发前就已经判空过了，而且已经加上clientname部分
//                    close(selector);
//                }else{
//                    System.out.println(msg);
//                }
            }
        }
    }
    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while(client.read(rBuffer)>0);
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }
    public void send(String msg) throws IOException {
        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while(wBuffer.hasRemaining()){
            client.write(wBuffer);//把wBuffer中的信息写入SocketChannel
        }
        if(isToQuit(msg)){
            close(selector);//selector的关闭是在这里！！！这一步是客户端退出的关键
        }
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
        ChatClient client = new ChatClient();
        client.start();
    }

}
