package org.wso2.extension.siddhi.io.feed.utils;

import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.impl.DefaultProvider;
import org.apache.abdera.protocol.server.impl.SimpleWorkspaceInfo;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

public class JettyServer {
    public static final int DEFAULT_PORT = 9002;

    private final int port;
    private Server server;
    private DefaultProvider customerProvider;

    public JettyServer() {
        this(DEFAULT_PORT);
    }

    public JettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        server = new Server(port);
        setupAbdera("/");
        Context context = new Context(server, "/", Context.SESSIONS);
        context.addServlet(new ServletHolder(new AbderaServlet() {

            @Override
            protected Provider createProvider() {
                customerProvider.init(getAbdera(), null);
                return customerProvider;
            }
        }), "/");
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public static void main(String args[]) throws Exception {
        JettyServer server = new JettyServer(3098);
        server.setupAbdera("/");
        server.start();
        Thread.sleep(1200000);
        server.stop();
    }

    private void setupAbdera(String base) throws Exception {
        customerProvider = new DefaultProvider(base);

        CustomerAdapter ca = new CustomerAdapter();
        ca.setHref("news");

        SimpleWorkspaceInfo wi = new SimpleWorkspaceInfo();
        wi.setTitle("Customer Workspace");
        wi.addCollection(ca);

        customerProvider.addWorkspace(wi);
    }
}
