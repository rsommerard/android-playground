package fr.rsommerard.wifisendingmessagewithdiscovery;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread implements Runnable {

    private Handler mHandler;
    private ServerSocket mServerSocket;

    public ServerThread(ServerSocket serverSocket, Handler handler) {
        super();
        mServerSocket = serverSocket;
        mHandler = handler;
    }

    @Override
    public void run() {
        try {
            Message message = new Message();

            message.obj = "Device Port: " + mServerSocket.getLocalPort();
            mHandler.sendMessage(message);

            while(true) {
                Socket socket = mServerSocket.accept();

                message = new Message();
                message.obj = "Client connected";
                mHandler.sendMessage(message);

                InputStream inputStream = socket.getInputStream();
                byte[] buffer = new byte[1024];

                int bytes = inputStream.read(buffer);

                Log.d(MainActivity.TAG, new String(buffer, 0, bytes));

                message = new Message();
                message.obj = "Message received: " + new String(buffer, 0, bytes);
                mHandler.sendMessage(message);
                socket.close();

                message = new Message();
                message.obj = "Client disconnected";
                mHandler.sendMessage(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
