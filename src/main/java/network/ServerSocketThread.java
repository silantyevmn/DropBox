package network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * ru.silantyevmn.dropbox.network
 * Created by Михаил Силантьев on 02.02.2018.
 */
public class ServerSocketThread extends Thread{
    private int port;
    private int timeout;
    private ServerSocketThreadListener listener;
    private ServerSocket serverSocket;
    private Socket socket;

    public ServerSocketThread(ServerSocketThreadListener listener,String name,int port,int timeout){
        super(name);
        this.port=port;
        this.timeout=timeout;
        this.listener=listener;
        start();
    }
    @Override
    public void run() {
        //ЗАПУЩЕН НОВЫЙ ПОТОК
        listener.onStartServerSocketThread(this);
        try{
            serverSocket=new ServerSocket(port);
            serverSocket.setSoTimeout(timeout);
            // СТАРТОВАЛ новый ServerSocket
            listener.onCreateServerSocket(this);
            while (!isInterrupted()){
                try {
                    socket=serverSocket.accept();
                } catch (SocketTimeoutException e) {
                    //случился timeout exception
                    listener.onTimeoutException(this,e);
                    continue;
                }
                //создался новый socket
                listener.onCreateSocket(this,socket);
            }
        } catch (IOException e){
            //Exception
            listener.onExceptionServerSocketThread(this,e);
        } finally {
            try {
                if(socket!=null) socket.close();
                if(serverSocket!=null) serverSocket.close();
            } catch (IOException e) {
                listener.onExceptionServerSocketThread(this,e);
            }
            // Поток остановился
            listener.onStopServerSocketThread(this);
        }
    }
}
