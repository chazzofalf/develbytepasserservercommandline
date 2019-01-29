
package com.webuildsystems.chazzofalf.bytePasserServer.ByteTosserServer;

import java.io.IOException;
import java.io.InputStream;

public class HolderInputStream
extends InputStream {
    private InputStream innerInputStream = null;

    @Override
    public int read() throws IOException {
        while (this.innerInputStream == null) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.innerInputStream.read();
    }

    @Override
    public void close() {
        if (this.innerInputStream != null) {
            try {
                this.innerInputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setInputStream(InputStream in) {
        this.innerInputStream = in;
    }
}

