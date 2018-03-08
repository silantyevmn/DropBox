package network;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * network
 * Created by Михаил Силантьев on 02.02.2018.
 */
public interface SocketThreadListener {
    void onStartSocketThread(SocketThread socketThread, Socket socket);
    void onSocketIsReady(SocketThread socketThread, Socket socket);
    //void onMessageString(SocketThread socketThread, Socket socket, String msg);

    void onException(SocketThread socketThread, Exception e);
    void onStopSocketThread(SocketThread socketThread);

    void onReadObject(SocketThread socketThread,Object obj);

}
