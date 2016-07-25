package com.brettonw.servlet;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;

public class TestServletInputStream extends ServletInputStream {
    @Override
    public boolean isFinished () {
        return false;
    }

    @Override
    public boolean isReady () {
        return false;
    }

    @Override
    public void setReadListener (ReadListener readListener) {

    }

    @Override
    public int read () throws IOException {
        return 0;
    }
}
