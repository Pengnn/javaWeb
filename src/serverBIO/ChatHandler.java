package serverBIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * @Description BIO模型中每个客户端对应一个ChatHandler，负责进行消息接收和发送
 * @Author Pengnan
 * @CreateTime 2021年04月11日 21:33:00
 */
public class ChatHandler implements Runnable{
    private ChatServer server;
    private Socket socket;
    public ChatHandler(ChatServer server,Socket socket){
        this.server=server;
        this.socket=socket;
    }
    @Override
    public void run() {
        try {
            //保存新上线的用户
            server.addClient(socket);
            //读取用户发送的消息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg=null;
            while((msg=reader.readLine())!=null){
                String fwdMsg="客户端["+socket.getPort()+"]:"+msg+"\n";
                System.out.println(fwdMsg);
                //将消息转发给聊天室的其他用户
                server.forwardMessage(socket,fwdMsg);
                //检查用户是否要退出
                if(server.isToQuit(msg)){
                    server.removeClient(socket);
                    break;
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }
}
