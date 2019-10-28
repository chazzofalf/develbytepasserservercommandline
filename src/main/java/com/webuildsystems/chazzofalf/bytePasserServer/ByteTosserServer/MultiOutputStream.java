
package com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

public class MultiOutputStream
extends OutputStream {
    private ArrayList<BufferedOutputStream> outputs = new ArrayList<>();
    private ArrayList<BufferedOutputStream> removeThese = new ArrayList<>();

    public void addOutputStream(OutputStream out) {
        OutputStream outIn = out;
        if (!(outIn instanceof BufferedOutputStream)) {
            outIn = new BufferedOutputStream(outIn);
        }
        this.outputs.add((BufferedOutputStream)outIn);
    }

    @Override
    public void write(int bytE) {
        for (BufferedOutputStream output : this.outputs) {
            try {
                output.write(bytE);
            }
            catch (IOException e) {
                try {
                    output.close();
                }
                catch (IOException e1) {
                    e1.printStackTrace();
                }
                this.removeThese.add(output);
            }
        }
        for (BufferedOutputStream remove : this.removeThese) {
            this.outputs.remove(remove);
        }
        this.removeThese.clear();
    }

    @Override
    public void write(byte[] buffer, int offset, int length) {
        for (int i = offset; i < offset + length; ++i) {
            this.write(buffer[i] & 255);
        }
    }

    @Override
    public void close() {
        for (BufferedOutputStream output : this.outputs) {
            try {
                output.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void write(byte[] buffer) {
        this.write(buffer, 0, buffer.length);
    }
}

