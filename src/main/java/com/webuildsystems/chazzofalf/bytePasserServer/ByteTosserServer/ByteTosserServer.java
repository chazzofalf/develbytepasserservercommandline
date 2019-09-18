
package com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.BufferedPipe;
import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.HolderInputStream;
import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.MultiOutputStream;
import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.PipeFinishedReadingHandler;

public class ByteTosserServer
implements PipeFinishedReadingHandler {
    private ServerSocket inputServerSocket;
    private ServerSocket outputServerSocket;
    private int inPort = -1;
    private int outPort = -1;
    private BufferedPipe pipe;
    private MultiOutputStream outs;
    private HolderInputStream holder;
    private boolean runServer = false;
    private Runnable inputServerRunnable = new Runnable(){

        @Override
        public void run() {
            ByteTosserServer.this.inputServerRunnableTask();
        }
    };
    private Runnable outputServerRunnable = new Runnable(){

        @Override
        public void run() {
            ByteTosserServer.this.outputServerRunnableTask();
        }
    };

    private MultiOutputStream getOuts() {
        if (this.outs == null) {
            this.outs = new MultiOutputStream();
        }
        return this.outs;
    }

    private HolderInputStream getHolder() {
        if (this.holder == null) {
            this.holder = new HolderInputStream();
        }
        return this.holder;
    }

    private BufferedPipe getPipe() {
        if (this.pipe == null) {
            this.pipe = new BufferedPipe();
            this.pipe.setInput(this.getHolder());
            this.pipe.setOutput(this.getOuts());
            this.pipe.setHandler(this);
        }
        return this.pipe;
    }

    public void setIncomingDataPort(int port) {
        this.inPort = port;
    }

    public void setOutgoingDataPort(int port) {
        this.outPort = port;
    }

    private void inputServerRunnableTask() {
        try {
            this.inputServerSocket = new ServerSocket(this.inPort);
            Socket inputSocket = this.inputServerSocket.accept();
            this.holder.setInputStream(inputSocket.getInputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputServerRunnableTask() {
        try {
            this.outputServerSocket = new ServerSocket(this.outPort);
            while (this.runServer) {
                try {
                    Socket outputSocket = this.outputServerSocket.accept();
                    this.getOuts().addOutputStream(outputSocket.getOutputStream());
                }
                catch (IOException outputSocket) {}
            }
        }
        catch (IOException outputSocket) {
            // empty catch block
        }
    }

    public void start() {
        this.runServer = true;
        this.getPipe().start();
        Thread input = new Thread(this.inputServerRunnable);
        Thread output =new Thread(this.outputServerRunnable);
        input.setDaemon(true);
        output.setDaemon(true);
        input.start();
        output.start();
    }

    public void stop() {
        this.getPipe().stop();
    }

    @Override
    public void pipeIsDone() {
        this.runServer = false;
        try {
            this.outputServerSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}

