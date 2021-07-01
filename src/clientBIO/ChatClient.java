package clientBIO;

import java.io.*;
import java.net.Socket;

/**
 * @Description 负责与服务器建立连接，以及发送消息给服务器，接收来自服务器的消息
 * @Author Pengnan
 * @CreateTime 2021年04月11日 21:30:00
 */
public class ChatClient {
    private final String DEFAULT_SERVER_HOST="127.0.0.1";
    private final int DEFAULT_SERVER_PORT=9999;
    private final String QUIT="quit";

    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;

    public void send(String msg) throws IOException {
        if(!socket.isOutputShutdown()){
            writer.write(msg+"\n");
            writer.flush();
        }
    }

    //接收来自服务器的消息
    public String receive() throws IOException {
        String msg=null;
        if(!socket.isInputShutdown()){
            msg = reader.readLine();
        }
        return msg;
    }

    public boolean isToQuit(String msg){
        return QUIT.equals(msg);
    }
    public void close(){
        if(writer!=null){
            try {
                writer.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void start(){
        try {
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);
            reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            //用户输入
            new Thread(new UserInputHandler(this)).start();//用户输入肯定是要在另外的线程进行的
            //接收服务器转发的消息
            String msg=null;
            while((msg=receive())!=null){
                System.out.println(msg);
            }

        } catch (IOException exception) {
            exception.printStackTrace();
        }finally {
            close();
        }
    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.start();
    }

}
