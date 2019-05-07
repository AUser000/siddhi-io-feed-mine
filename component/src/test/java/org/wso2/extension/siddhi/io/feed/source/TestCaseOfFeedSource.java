/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.io.feed.source;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestCaseOfFeedSource {
    private Logger log = Logger.getLogger(TestCaseOfFeedSource.class.getName());
    private AtomicBoolean eventArrived = new AtomicBoolean(false);

    @AfterMethod
    public void setEventArrivedFalse() {
        eventArrived.set(false);
    }

    // https://wso2.org/jenkins/job/siddhi/job/siddhi-io-tcp/rssAll
    // http://feeds.bbci.co.uk/news/rss.xml
    // http://rss.cnn.com/rss/edition.rss
    // http://feeds.bbci.co.uk/news/rss.xml#
    // http://localhost:9002/employee
    // examples for rss(atom & rss) feed end points

    @Test
    public void sourceForAtom() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           Feed Source For Atom Test                               ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@source(type='feed', \n" +
                "url = 'https://wso2.org/jenkins/job/siddhi/job/siddhi-io-tcp/rssAll', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false'), \n" +
                "feed.type = 'atom') \n" +
                " define stream inputStream(link string, title string, id string, published string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event: events) {
                    eventArrived.set(true);
                }
            }
        });

        executionPlanRuntime.start();
        Thread.sleep(5000);
        siddhiManager.shutdown();
        Assert.assertTrue(eventArrived.get());
    }


    @Test
    public void sourceForPauseAndResume() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                  Feed Source For Pause and Resume Test                               ");
        log.info("-------------------------------------------------------------------------------------");

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
                for (Event event: events) {
                    eventArrived.set(true);
                }
            }
        });
        executionPlanRuntime.start();
        Thread.sleep(5500);
        eventArrived.set(false);
        sources.forEach(e -> e.forEach(Source::pause));
        Thread.sleep(55000);
        sources.forEach(e -> e.forEach(Source::resume));
        Thread.sleep(5500);
        siddhiManager.shutdown();
        Assert.assertTrue(eventArrived.get());
    }

    @Test
    public void sourceForRss() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                           Feed Source For RSS Test                               ");
        log.info("-------------------------------------------------------------------------------------");
        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@source(type='feed', \n" +
                "url = 'http://feeds.bbci.co.uk/news/rss.xml', \n" +
                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false'), \n" +
                "feed.type = 'rss') \n" +
                " define stream inputStream(title string, id string, pubDate string);\n";

        SiddhiAppRuntime executionPlanRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        executionPlanRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                for (Event event: events) {
                    eventArrived.set(true);
                }
            }
        });
        executionPlanRuntime.start();
        Thread.sleep(4000);
        siddhiManager.shutdown();
        Assert.assertTrue(eventArrived.get());
    }


    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void sinkForValidation() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                Feed Source For Feed Type Validation Test                            ");
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

    }

    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void sinkForValidation2() throws InterruptedException {

        log.info("-------------------------------------------------------------------------------------");
        log.info("                  Feed Source For Request Interval Validation Test                   ");
        log.info("-------------------------------------------------------------------------------------");

        SiddhiManager siddhiManager = new SiddhiManager();

        String siddhiApp = "@App:name('test') \n" +

                "@Source(type='feed', \n" +
                "feed.type = 'Atom', \n" +
                "request.interval = '-1', \n" +
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

    }
}

