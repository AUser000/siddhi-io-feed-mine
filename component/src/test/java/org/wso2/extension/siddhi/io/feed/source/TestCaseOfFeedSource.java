package org.wso2.extension.siddhi.io.feed.source;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.io.feed.utils.PortAllocator;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class TestCaseOfFeedSource {
    private Logger log = Logger.getLogger(TestCaseOfFeedSource.class.getName());
    private int port;


    //@BeforeClass
    public void startServer() {
        port = PortAllocator.allocatePort();
        try {
            //AtomPubServer server = new AtomPubServer(3000);
        } catch (Exception e) {
            log.info(e);
        }
    }

    @Test
    public void sourceForAtom() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           SNMP Version 1 Basic Source                               ");
        log.info("-------------------------------------------------------------------------------------");

        // https://wso2.org/jenkins/job/siddhi/job/siddhi-io-tcp/rssAll
        // http://feeds.bbci.co.uk/news/rss.xml
        // http://rss.cnn.com/rss/edition.rss
        // http://feeds.bbci.co.uk/news/rss.xml#
        // http://localhost:9002/employee
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@source(type='feed', \n" +
                "url = 'https://wso2.org/jenkins/job/siddhi/job/siddhi-io-tcp/rssAll', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false'), \n" +
                "request.interval = '1', \n" +
                "feed.type = 'atom') \n" +
                " define stream inputStream(link string, title string, id string, published string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });
        //InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");


        executionPlanRuntime.start();
        Thread.sleep(5000);
        siddhiManager.shutdown();
    }


    @Test
    public void sourceForPauseAndResume() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           SNMP Version 1 Basic Source                               ");
        log.info("-------------------------------------------------------------------------------------");

        // https://wso2.org/jenkins/job/siddhi/job/siddhi-io-tcp/rssAll
        // http://feeds.bbci.co.uk/news/rss.xml
        // http://rss.cnn.com/rss/edition.rss
        // http://feeds.bbci.co.uk/news/rss.xml#
        // http://localhost:9002/employee
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@source(type='feed', \n" +
                "url = 'https://wso2.org/jenkins/job/siddhi/job/siddhi-io-tcp/rssAll', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false'), \n" +
                "request.interval = '1', \n" +
                "feed.type = 'atom') \n" +
                " define stream inputStream(link string, title string, id string, published string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        Collection<List<Source>> sources = executionPlanRuntime.getSources();
        executionPlanRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });
        //InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");


        executionPlanRuntime.start();
        //Thread.sleep(55000);
        sources.forEach(e -> e.forEach(Source::pause));
        Thread.sleep(6000);
        sources.forEach(e -> e.forEach(Source::resume));
        siddhiManager.shutdown();
    }

    @Test
    public void sourceForRss() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           SNMP Version 1 Basic Source                               ");
        log.info("-------------------------------------------------------------------------------------");

        // https://wso2.org/jenkins/job/siddhi/job/siddhi-io-tcp/rssAll
        // http://feeds.bbci.co.uk/news/rss.xml
        // http://rss.cnn.com/rss/edition.rss
        // http://feeds.bbci.co.uk/news/rss.xml#
        // http://localhost:9002/employee
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@source(type='feed', \n" +
                "url = 'http://feeds.bbci.co.uk/news/rss.xml', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false'), \n" +
                "request.interval = '1', \n" +
                "feed.type = 'rss') \n" +
                " define stream inputStream(title string, id string, updated string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
            }
        });
        executionPlanRuntime.start();
        Thread.sleep(5000);
        siddhiManager.shutdown();
    }


    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void sinkForValidation() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           FEED Sink Test                                            ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@Source(type='feed', \n" +
                "feed.type = 'Atomm', \n" +
                "url = 'http://feeds.bbci.co.uk/news/rss.xml', \n" +
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
}

