package Test;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @Description AIO 回音壁客户端实现
 * @Author Pengnan
 * @CreateTime 2021年04月13日 14:46:00
 */
public class ClientAIO {
    private final String LOCALHOST="localhost";
    private final int DEFAULT_PORT=8888;

    AsynchronousSocketChannel clientChannel;
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
            clientChannel = AsynchronousSocketChannel.open();
            Future<Void> future = clientChannel.connect(new InetSocketAddress(LOCALHOST, DEFAULT_PORT));
            future.get();
            //等待用户输入
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                 String in = consoleReader.readLine();
                 byte[] inBytes = in.getBytes();
                 ByteBuffer buffer = ByteBuffer.wrap(inBytes);
                 buffer.flip();//切换到读模式
                 Future<Integer> writeResult = clientChannel.write(buffer);//写入channel
                 writeResult.get();//阻塞式调用等待结果返回
                 buffer.clear();//切换到写模式
                Future<Integer> readResult = clientChannel.read(buffer);//从channel中读到buffer
                 readResult.get();
                 System.out.println(new String(buffer.array()));
               //  buffer.clear();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }finally {
            close(clientChannel);
        }
    }

    public static void main(String[] args) {
        final ClientAIO clientAIO = new ClientAIO();
        clientAIO.start();
    }
}
