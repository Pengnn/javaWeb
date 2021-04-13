import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @Description 四种文件传输方式
 * @Author Pengnan
 * @CreateTime 2021年04月12日 14:38:00
 */
interface FileCopyRunner{
    void copyFile(File source, File target);
}
public class FileCopyDemo {
    private static final int ROUNDS=10;
    private static void benchmark(FileCopyRunner test,File source,File target){
        long elapsed=0L;
        for(int i=0;i<ROUNDS;i++){
            long startTime = System.currentTimeMillis();
            test.copyFile(source,target);
            elapsed+=System.currentTimeMillis()-startTime;
            target.delete();
        }
        System.out.println(test+":"+elapsed/ROUNDS);
    }
    private static void close(Closeable closeable){
        if(closeable!=null){
            try {
                closeable.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        FileCopyRunner noBufferStreamCopy=new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin=null;
                OutputStream fout=null;
                try {
                    fin=new FileInputStream(source);
                    fout=new FileOutputStream(target);
                    int len;
                    while((len=fin.read())!=1){
                        fout.write(len);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "noBufferStreamCopy";
            }
        };
        FileCopyRunner bufferStreamCopy=new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                InputStream fin=null;
                OutputStream fout=null;

                try {
                    fin=new BufferedInputStream(new FileInputStream(source));
                    fout=new BufferedOutputStream(new FileOutputStream(target));
                    byte[] buffer = new byte[1024];
                    int len;
                    while((len=fin.read(buffer))!=-1){
                        fout.write(buffer,0,len);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "bufferStreamCopy";
            }
        };
        FileCopyRunner nioChannelBufferCopy=new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin=null;
                FileChannel fout=null;

                try {
                    fin=new FileInputStream(source).getChannel();
                    fout=new FileOutputStream(target).getChannel();

                    ByteBuffer buffer= ByteBuffer.allocate(1024);
                    while(fin.read(buffer)!=-1){//写模式
                       //切换到读模式
                       buffer.flip();
                       while(buffer.hasRemaining()){
                           fout.write(buffer);
                       }
                       //读模式切换到写模式
                        buffer.clear();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "nioChannelBufferCopy";
            }
        };
        FileCopyRunner nioChannelTransferCopy=new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                FileChannel fin=null;
                FileChannel fout=null;

                try {
                    fin=new FileInputStream(source).getChannel();
                    fout=new FileOutputStream(target).getChannel();
                    long len=0L;
                    while(len<fin.size()) {
                        len = +fin.transferTo(0, fin.size(), fout);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException exception) {
                    exception.printStackTrace();
                }finally {
                    close(fin);
                    close(fout);
                }
            }

            @Override
            public String toString() {
                return "nioChannelTransferCopy";
            }
        };
        File source=new File("D:\\Program Files\\spring-5.3.2-dist.zip");
        File target=new File("target.zip");

        System.out.println("-----Copying file-----");
        benchmark(noBufferStreamCopy,source,target);
        benchmark(bufferStreamCopy,source,target);
        benchmark(nioChannelBufferCopy,source,target);
        benchmark(nioChannelTransferCopy,source,target);
       // nioChannelBufferCopy.copyFile(source,target);
    }

}
