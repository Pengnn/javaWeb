package Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Description TCP服务端接收文件
 * @Author Pengnan
 * @CreateTime 2021年04月06日 11:15:00
 */
public class TcpServiceDemo02 {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(9000);
        Socket socket = serverSocket.accept();
        InputStream is = socket.getInputStream();

        FileOutputStream fos = new FileOutputStream(new File("receive.jpg"));
        byte[] buffer = new byte[1024];
        int len;
        while((len=is.read(buffer))!=-1){
            fos.write(buffer,0,len);
        }

        OutputStream os = socket.getOutputStream();
        os.write("服务器接收完毕了，客户端可以断开了".getBytes());

        os.close();
        fos.close();
        is.close();
        socket.close();
        serverSocket.close();
    }
}
