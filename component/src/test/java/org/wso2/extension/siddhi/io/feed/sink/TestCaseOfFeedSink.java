package org.wso2.extension.siddhi.io.feed.sink;

import org.apache.abdera.protocol.server.provider.basic.BasicProvider;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.feed.source.TestCaseOfFeedSource;
import org.wso2.extension.siddhi.io.feed.utils.JettyServer;
import org.wso2.extension.siddhi.io.feed.utils.PortAllocator;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;

import java.util.Date;

public class TestCaseOfFeedSink {
    // If you will know about this related testcase,
    //refer https://github.com/wso2-extensions/siddhi-io-file/blob/master/component/src/test
    private Logger log = Logger.getLogger(TestCaseOfFeedSource.class.getName());
    JettyServer server;
    private int port = 3098;

    //@BeforeClass
    public void startServer() {
        port = PortAllocator.allocatePort();
        try {
            server = new JettyServer(port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void basicSink() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Sink Test                               ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = 'http://localhost:"+port+"/news', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(id string, published string, content string, title string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("outputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{"http://localhost:3000/sample/foo", new Date(), "name", "hello"});
        inputHandler.send(new Object[]{"http://localhost:3000/sample/bar", new Date(), "nmae", "helloow"});


        siddhiManager.shutdown();
    }

    @Test
    public void sinkForHTTPS() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Sink Test                               ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = 'http://localhost:"+port+"/news', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(id string, published string, content string, title string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("outputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{"http://localhost:3000/sample/foo", new Date(), "name", "hello"});
        inputHandler.send(new Object[]{"http://localhost:3000/sample/bar", new Date(), "nmae", "helloow"});


        siddhiManager.shutdown();
    }

    //@AfterClass
    public void stopServer() throws Exception {
        server.stop();
    }

}

