package Test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @Description TCP客户端发送文件
 * @Author Pengnan
 * @CreateTime 2021年04月06日 11:15:00
 */
public class TcpClientDemo02 {
    public static void main(String[] args) throws Exception {
        Socket socket = new Socket(InetAddress.getByName("127.0.0.1"), 9000);
        OutputStream os = socket.getOutputStream();

        FileInputStream fio = new FileInputStream(new File("test.jpg"));
        byte[] buffer = new byte[1024];
        int len;
        while((len=fio.read(buffer))!=-1){
            os.write(buffer,0,len);
        }

        socket.shutdownOutput();//通知服务器，客户端已经发送完毕

        InputStream is = socket.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer2 = new byte[1024];
        int len2;
        while((len2=is.read(buffer))!=-1){
            baos.write(buffer2,0,len2);
        }
        System.out.println(baos.toString());

        baos.close();
        is.close();
        os.close();
        socket.close();

    }
}
