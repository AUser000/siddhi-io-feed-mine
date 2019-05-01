package org.wso2.extension.siddhi.io.feed.sink;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
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
    private Logger log = Logger.getLogger(TestCaseOfFeedSource.class.getName());
    private static JettyServer server;
    private static Abdera abdera = Abdera.getInstance();
    private static AbderaClient client = new AbderaClient();

    private static String base;
    private static String feedBase;

    @BeforeClass
    public void startServer() throws Exception {
        int port = 3000;//PortAllocator.allocatePort();
        server = new JettyServer(port);
        server.start();//(CustomProvider.class);
        base = "http://localhost:" + port + "/news";
        feedBase = base;// + "/feed";

    }

    @AfterClass
    public void stopServer() throws Exception {
        //Thread.sleep(600000);
        server.stop();
    }

    @Test
    public void basicSink() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Create Sink Test                               ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '"+ feedBase +"', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(id string, title string, content string, link string, author string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("outputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{base +"/feed/entries/1", "", "Content", "sample.com", "David Becom"});
        inputHandler.send(new Object[]{base +"/feed/entries/2", "", "Content", "example.com", "Renaldo Axio"});
        inputHandler.send(new Object[]{base +"/feed/entries/3", "", "Content", "example7.com", "Renaldo-Axio"});
        inputHandler.send(new Object[]{base +"/feed/entries/2", "", "Content", "example.com", "Renaldo Axio"});
//        inputHandler.send(new Object[]{base +"/feed/entries/2", "Test Title 2", "Content", "example.com", "Renaldo Axio"});
//        inputHandler.send(new Object[]{base +"/feed/entries/2", "Test Title 2", "Content", "example.com", "Renaldo Axio"});


        siddhiManager.shutdown();
    }

    @Test(enabled = false)
    public void sinkForUpdate() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Sink Test                               ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '"+base+"', \n" +
                "http.response.code = '200', \n" +
                "atom.func = 'update', \n" +
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
        inputHandler.send(new Object[]{"1001", new Date(), "name", "hello"});
        siddhiManager.shutdown();
    }

    @Test(dependsOnMethods = "basicSink")
    public void sinkForDelete() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Sink Test                               ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '"+feedBase+"', \n" +
                "http.response.code = '204', \n" +
                "atom.func = 'delete', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(id string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("outputStream");
        executionPlanRuntime.start();

        ClientResponse resp = client.get(feedBase);
        Document<Feed> feed_doc = resp.getDocument();
        Feed feed = feed_doc.getRoot();
        System.out.println("before the deleting");
        for(Entry entry4:feed.getEntries()) {
            System.out.println(entry4.getId().toString());
        }
        Entry entry = feed.getEntries().get(0);

        String edit = entry.getEditLinkResolvedHref().toString();
        String edit2 = entry.getEditLinkResolvedHref().toString();
        System.out.println("link to edit  ---> " + edit);


        resp.release();

        System.out.println("trying to remove " + edit);
        //inputHandler.send(new Object[]{edit});
        inputHandler.send(new Object[]{edit});
        //inputHandler.send(new Object[]{"http://localhost:3000/sample/bar"});


        siddhiManager.shutdown();

        System.out.println("after the deleting");
        ClientResponse resp2 = client.get(feedBase);
        Document<Feed> feed_doc2 = resp2.getDocument();
        Feed feed2 = feed_doc2.getRoot();
        for(Entry entry3:feed2.getEntries()) {
            System.out.println(entry3.getId().toString());
        }
        resp.release();
    }


    @Test(enabled = false)
    public void sinkForHTTPS() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Sink Test                                            ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '"+base+"', \n" +
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

        inputHandler.send(new Object[]{"feed/entries/1", new Date(), "name", "hello"});
        inputHandler.send(new Object[]{"feed/entries/1", new Date(), "name", "hello"});
        //inputHandler.send(new Object[]{"http://localhost:3000/sample/bar", new Date(), "nmae", "helloow"});


        siddhiManager.shutdown();
    }

}

