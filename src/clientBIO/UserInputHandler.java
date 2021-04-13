package clientBIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @Description 负责给服务器端发送用户输入的消息
 * @Author Pengnan
 * @CreateTime 2021年04月11日 21:32:00
 */
public class UserInputHandler implements Runnable{
    private ChatClient chatClient;

    public UserInputHandler(ChatClient chatClient){
        this.chatClient=chatClient;
    }

    @Override
    public void run() {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            while(true) {
                String msg = consoleReader.readLine();
                //向服务器发送消息
                chatClient.send(msg);
                //检查是否要退出
                if(chatClient.isToQuit(msg)){
                    break;
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

    }
}
