package org.wso2.extension.siddhi.io.feed.sink;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.client.AbderaClient;
import org.apache.abdera.protocol.client.ClientResponse;
import org.apache.log4j.Logger;
import org.testng.Assert;
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
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Date;

public class TestCaseOfFeedSink {
    private Logger log = Logger.getLogger(TestCaseOfFeedSource.class.getName());
    private static JettyServer server;
    private static Abdera abdera = Abdera.getInstance();
    private static AbderaClient client = new AbderaClient();

    private static String base;

    @BeforeClass
    public void startServer() throws Exception {
        int port = PortAllocator.allocatePort();
        server = new JettyServer(port);
        server.start();
        base = "http://localhost:" + port + "/news";
    }

    @AfterClass
    public void stopServer() throws Exception {
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
                "url = '" + base + "', \n" +
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

        inputHandler.send(new Object[]{base + "/feed/entries/1", "Title1", "Content", "sample.com", "David Becom"});
        inputHandler.send(new Object[]{base + "/feed/entries/2", "Title2", "Content", "example.com", "Renaldo Axio"});
        inputHandler.send(new Object[]{base + "/feed/entries/3", "Title3", "Content", "example7.com", "Renaldo-Axio"});
        inputHandler.send(new Object[]{base + "/feed/entries/2", "Title4", "Content", "example.com", "Renaldo Axio"});

        siddhiManager.shutdown();

        ClientResponse resp = client.get(base);
        Document<Feed> feedDoc = resp.getDocument();
        Feed feed = feedDoc.getRoot();
        Entry entry = feed.getEntries().get(0);
        Assert.assertTrue("Content".equals(entry.getContent()));

    }

    @Test(dependsOnMethods = "basicSink")
    public void sinkForUpdate() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                             FEED Sink UPDATE Test                                   ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        ClientResponse resp = client.get(base);
        Document<Feed> feedDoc = resp.getDocument();
        Feed feed = feedDoc.getRoot();
        Entry entry = feed.getEntries().get(1);

        String editLink = entry.getEditLinkResolvedHref().toString();
        resp.release();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '" + editLink + "', \n" +
                "http.response.code = '204', \n" +
                "atom.func = 'update', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(content string, title string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("outputStream");
        executionPlanRuntime.start();

        inputHandler.send(new Object[]{"XXXXXXX", "AAAAAA"});
        siddhiManager.shutdown();

        resp = client.get(base);
        feedDoc = resp.getDocument();
        feed = feedDoc.getRoot();
        Entry entry1 = feed.getEntries().get(1);

        Assert.assertTrue(entry1.getTitle().equals("AAAAAA"));
    }

    @Test(dependsOnMethods = "sinkForUpdate")
    public void sinkForDelete() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Sink DELETE Test                               ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '" + base + "', \n" +
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

        ClientResponse resp = client.get(base);
        Document<Feed> feedDoc = resp.getDocument();
        Feed feed = feedDoc.getRoot();
        Entry entry = feed.getEntries().get(0);

        String edit = entry.getEditLinkResolvedHref().toString();
        resp.release();

        inputHandler.send(new Object[]{edit});
        siddhiManager.shutdown();

        ClientResponse resp2 = client.get(base);
        Document<Feed> feedDoc2 = resp2.getDocument();
        Feed feed2 = feedDoc2.getRoot();
        Entry entry2 = feed2.getEntries().get(0);

        Assert.assertFalse(entry.getId().equals(entry2.getId()));
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
                "url = '" + base + "', \n" +
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


        siddhiManager.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void sinkForValidation() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Url Malformed                                            ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@Sink(type='feed', \n" +
                "feed.type = 'Atomm', \n" +
                "url = 'localhost.', \n" +
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


        siddhiManager.shutdown();
    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void sinkValidationTest() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                             FEED Sink UPDATE Test                                   ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@sink(type='feed', \n" +
                "url = '" + base + "', \n" +
                "http.response.code = '204', \n" +
                "atom.func = 'error value', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false')) \n" +
                " define stream outputStream(content string, title string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("outputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });
    }
}

