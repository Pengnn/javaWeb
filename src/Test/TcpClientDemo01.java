package Test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;


/**
 * @Description 客户端
 * @Author Pengnan
 * @CreateTime 2021年04月10日 10:39:00
 */
public class TcpClientDemo01 {
    public static void main(String[] args) {
        final String QUIT="quit";//通信结束的关键词
        BufferedWriter writer=null;
        BufferedReader reader=null;
       try {
            Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 9999);
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            while(true){
                //客户端发送消息给服务器
                String inMsg=in.readLine();
                writer.write(inMsg+"\n");
                writer.flush();
                if(QUIT.equals(inMsg)){
                    break;
                }
                //接收来自服务器的消息
                String msg = reader.readLine();
                System.out.println(msg);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }finally {
            if(writer!=null){
                try {
                    writer.close();
                    System.out.println("客户端socket关闭");
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
