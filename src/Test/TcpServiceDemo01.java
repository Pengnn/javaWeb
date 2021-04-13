package Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description 服务器
 * @Author Pengnan
 * @CreateTime 2021年04月10日 10:39:00
 */
public class TcpServiceDemo01 {
    public static void main(String[] args)  {
        final String QUIT="quit";
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(9999);
            BufferedWriter writer=null;
            BufferedReader reader=null;
            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("客户端["+socket.getPort()+"]连接");
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                String msg=null;
                while((msg=reader.readLine())!=null){
                    //接收来自客户端的消息
                   System.out.println("客户端["+socket.getPort()+"]:"+msg);
                        if(QUIT.equals(msg)){
                            System.out.println("客户端["+socket.getPort()+"]断开");
                            break;
                        }
                        //回复客户端消息
                        writer.write("服务器回复："+msg+"\n");
                        writer.flush();
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }finally {
            if(serverSocket!=null){
                try {
                    serverSocket.close();
                    System.out.println("服务器socket关闭");
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
