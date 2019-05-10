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

import org.wso2.extension.siddhi.io.feed.utils.Constants;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Source implementation of feed
 */

@Extension(
        name = "feed",
        namespace = "source",
        description = " The feed source allows user to make request and get feed entries from Rss(Atom and Rss) " +
                "servers periodically. This source can consume both RSS and Atom type feed entries. ",
        parameters = {
                @Parameter(name = Constants.URL,
                        description = "address of the feed end point",
                        type = DataType.STRING),
                @Parameter(name = Constants.FEED_TYPE,
                        description = "Type of the feed. Acceptance parameters are Rss and Atom",
                        type = DataType.STRING),
                @Parameter(name = Constants.REQUEST_INTERVAL,
                        description = "request interval in minutes",
                        optional = true,
                        defaultValue = Constants.DEFAULT_REQUEST_INTERVAL,
                        type = DataType.INT)
        },
        examples = {
                @Example(
                        syntax = "@App:name('test')\n" +
                                "@source(type='feed',\n" +
                                "url = 'http://feeds.bbci.co.uk/news/rss.xml',\n" +
                                "@map(type = 'keyvalue', fail.on.missing.attribute = 'false'),\n" +
                                "request.interval = '15',\n" +
                                "feed.type = 'rss')\n" +
                                " define stream inputStream(title string, id string, updated string)",
                        description = " This Query Shows how request to the http server and consume Rss feed entries " +
                                "with in 15min "
                )
        }
)

public class FeedSource extends Source {
    OptionHolder optionHolder;
    private URL url;
    private String type;
    private int requestInterval;
    private FeedListener listener;
    private ScheduledFuture future;
    private SourceEventListener sourceEventListener;
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {

        this.optionHolder = optionHolder;
        this.sourceEventListener = sourceEventListener;
        try {
            url = new URL(this.optionHolder.validateAndGetStaticValue(Constants.URL));
        } catch (MalformedURLException e) {
            throw new SiddhiAppValidationException(" Url format Error in siddhi stream " +
                    sourceEventListener.getStreamDefinition().getId());
        }
        this.type = validateType();
        this.requestInterval = validateRequestInterval();
        this.scheduledExecutorService = siddhiAppContext.getScheduledExecutorService();
    }

    private String validateType() {
        String type = optionHolder.validateAndGetStaticValue(Constants.FEED_TYPE);
        type = type.toLowerCase(Locale.ENGLISH);
        if (type.equals(Constants.RSS)) {
            return type;
        } else if (type.equals(Constants.ATOM)) {
            return type;
        } else {
            throw new SiddhiAppValidationException("Feed Type Validation error in "
                    + sourceEventListener.getStreamDefinition().getId() + " Acceptance parameters are RSS & Atom");
        }
    }

    private int validateRequestInterval() {
        try {
            int requestInterval = Integer.parseInt(optionHolder.validateAndGetStaticValue(Constants.REQUEST_INTERVAL,
                    Constants.DEFAULT_REQUEST_INTERVAL));
            if (requestInterval > 0) {
                return requestInterval;
            } else {
                throw new SiddhiAppValidationException(" Error in " + sourceEventListener.getStreamDefinition().getId()
                        + " validating request interval, Request interval accept only positive integers ");
            }
        } catch (NumberFormatException e) {
            throw new SiddhiAppValidationException(" Error in " + sourceEventListener.getStreamDefinition().getId()
                    + " validating request interval, Request interval accept only positive integers ");
        }
    }


    @Override
    public Class[] getOutputEventClasses() {
        return new Class[] {Map.class};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        listener = new FeedListener(sourceEventListener, url, type);
        future = scheduledExecutorService.scheduleAtFixedRate(listener, 0, requestInterval, TimeUnit.MINUTES);
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {
        if (future != null) {
            future.cancel(true);
        }
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
        }
    }

    @Override
    public void pause() {
        listener.pause();
    }

    @Override
    public void resume() {
        listener.resume();
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {

    }
}

