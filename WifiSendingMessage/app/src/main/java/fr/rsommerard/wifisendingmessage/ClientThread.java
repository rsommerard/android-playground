package fr.rsommerard.wifisendingmessage;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientThread extends Thread implements Runnable {

    private Handler mHandler;

    public ClientThread(Handler handler) {
        super();
        mHandler = handler;
    }

    @Override
    public void run() {
        //SocketAddress proxySocketAddress = new InetSocketAddress("cache.univ-lille1.fr", 3128);
        //Socket socket = new Socket(new Proxy(Proxy.Type.SOCKS, proxySocketAddress));

        Socket socket = new Socket();

        String serverAddress = "10.201.0.189";
        int serverPort = 33455;

        try {
            socket.connect(new InetSocketAddress(serverAddress, serverPort), 5000);

            Message handlerMessage = new Message();
            handlerMessage.obj = "Server connected";
            mHandler.sendMessage(handlerMessage);

            OutputStream outputStream = socket.getOutputStream();

            String message = "This is a test!";

            handlerMessage = new Message();
            handlerMessage.obj = "Sending message: " + message;
            mHandler.sendMessage(handlerMessage);

            byte[] buffer = message.getBytes();
            outputStream.write(buffer);

            socket.close();

            handlerMessage = new Message();
            handlerMessage.obj = "Server disconnected";
            mHandler.sendMessage(handlerMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
