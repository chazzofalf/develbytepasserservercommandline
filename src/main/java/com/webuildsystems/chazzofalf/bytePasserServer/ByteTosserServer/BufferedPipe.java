
package com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.MultiOutputStream;
import com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer.PipeFinishedReadingHandler;

public class BufferedPipe {
    private PipeFinishedReadingHandler handler;
    private InputStream in;
    private OutputStream out;
    private boolean runPipeThreadRunnable = false;
    private Runnable pipeThreadRunnable = new Runnable(){

        @Override
        public void run() {
            BufferedPipe.this.pipeThreadRunnableTask();
        }
    };

    public void setHandler(PipeFinishedReadingHandler handler) {
        this.handler = handler;
    }

    private void pipeThreadRunnableTask() {
        int readIn = 0;
        try {
            while (this.runPipeThreadRunnable & (readIn = this.in.read()) != -1) {
                this.out.write(readIn);
            }
            this.runPipeThreadRunnable = false;
            this.in.close();
            this.out.flush();
            this.out.close();
            if (this.handler != null) {
                this.handler.pipeIsDone();
            }
        }
        catch (IOException e) {
            this.runPipeThreadRunnable = false;
            try {
                this.in.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                this.out.close();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public void setInput(InputStream in) {
        InputStream inIn = in;
        if (!(inIn instanceof BufferedInputStream)) {
            inIn = new BufferedInputStream(inIn);
        }
        this.in = inIn;
    }

    public void setOutput(OutputStream out) {
        OutputStream outIn = out;
        if (!(outIn instanceof MultiOutputStream)) {
        	this.out = new MultiOutputStream();
            MultiOutputStream outInTemp = (MultiOutputStream)this.out;
            outInTemp.addOutputStream(outIn);
            outIn = outInTemp;
            
        }
        else
        {
        	this.out = out;
        }
    }

    public void start() {
        this.runPipeThreadRunnable = true;
        new Thread(this.pipeThreadRunnable).start();
    }

    public void stop() {
        this.runPipeThreadRunnable = false;
    }

}

