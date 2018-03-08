package network;


import java.net.Socket;

/**
 * ru.silantyevmn.dropbox.network
 * Created by Михаил Силантьев on 02.02.2018.
 */
public interface ServerSocketThreadListener {
    void onStartServerSocketThread(ServerSocketThread thread);
    void onStopServerSocketThread(ServerSocketThread thread);
    void onExceptionServerSocketThread(ServerSocketThread thread, Exception e);
    void onTimeoutException(ServerSocketThread thread, Exception e);

    void onCreateServerSocket(ServerSocketThread thread);

    void onCreateSocket(ServerSocketThread thread, Socket socket);
}
