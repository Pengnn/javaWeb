package serverBIO;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description 监听客户端的连接请求，建立连接，保存所用在线用户集合
 * @Author Pengnan
 * @CreateTime 2021年04月11日 21:35:00
 */
public class ChatServer {
    private int DEFAULT_PORT=9999;
    private final String QUIT="quit";

    private ExecutorService executorService;
    private ServerSocket serverSocket;
    private Map<Integer, Writer> connectedClients;

    public ChatServer(){
        executorService = Executors.newFixedThreadPool(10);
        this.connectedClients=new HashMap<>();
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if(socket!=null){
            int port = socket.getPort();
            BufferedWriter writer =
                    new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            connectedClients.put(port,writer);
            System.out.println("客户端["+port+"]已连接到服务器");
        }
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if(socket!=null){
            int port = socket.getPort();
            if(connectedClients.containsKey(port)){
                connectedClients.get(port).close();//关闭服务器端下线用户对应的输出流
                connectedClients.remove(port);//移除下线用户
                System.out.println("客户端["+port+"]已断开连接");
            }
        }
    }

    public synchronized void forwardMessage(Socket socket,String fwdMsg) throws IOException {
        for(Integer id:connectedClients.keySet()){
            if(!id.equals(socket.getPort())){
                Writer writer = connectedClients.get(id);
                writer.write(fwdMsg);
                writer.flush();
            }
        }
    }

    public boolean isToQuit(String msg){
        return QUIT.equals(msg);
    }
    public void close(){
        if(serverSocket!=null){
            try {
                serverSocket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    public void start(){
        try {
            ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
            System.out.println("启动服务器，监听端口"+DEFAULT_PORT+"...");
            while(true){
                //等待客户端的连接
                Socket socket = serverSocket.accept();//从已建立连接的队列中取出一个连接，如果没有就阻塞
                //创建ChatHandler线程
                executorService.execute(new ChatHandler(this,socket));//每个连接都开启一个线程
               // new Thread(new ChatHandler(this,socket)).start();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start();
    }

}
