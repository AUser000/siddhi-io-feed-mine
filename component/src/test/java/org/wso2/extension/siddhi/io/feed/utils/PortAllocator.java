package org.wso2.extension.siddhi.io.feed.utils;

import java.io.IOException;
import java.net.ServerSocket;

public class PortAllocator {
    public static int allocatePort() {

        try {
            ServerSocket ss = new ServerSocket(0);
            int port = ss.getLocalPort();
            ss.close();
            return port;
        } catch (IOException e) {
            throw new Error("Unable to allocate TCP port", e);
        }
    }
}
