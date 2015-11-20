package fr.rsommerard.wifisendingmessagewithdiscovery;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientThread extends Thread implements Runnable {

    private InetAddress mServerAddress;
    private int mServerPort;
    private Handler mHandler;

    public ClientThread(InetAddress serverAddress, int serverPort, Handler handler) {
        super();
        mHandler = handler;
        mServerAddress = serverAddress;
        mServerPort = serverPort;
    }

    @Override
    public void run() {
        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(mServerAddress, mServerPort), 5000);

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