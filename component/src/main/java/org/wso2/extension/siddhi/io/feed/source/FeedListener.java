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

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.feed.utils.Constants;
import org.wso2.extension.siddhi.io.feed.utils.EntryUtils;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a sample class-level comment, explaining what the extension class does.
 */
public class FeedListener implements Runnable {
    Logger log = Logger.getLogger(FeedSource.class);
    private boolean paused = false;
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private SourceEventListener sourceEventListener;
    private URL url;
    private Abdera abdera;
    private String type;

    public FeedListener(SourceEventListener sourceEventListener, URL url,
                        String type) throws IOException {
        this.sourceEventListener = sourceEventListener;
        this.url = url;
        abdera = new Abdera();
        this.type = type;
    }

    @Override
    public void run() {
        try {
            URLConnection urlConnection = url.openConnection();
            if (type.equals(Constants.ATOM)) {
                Document<Feed> doc = abdera.getParser().parse(urlConnection.getInputStream(), url.toString());
                Feed feed = doc.getRoot();
                for (Entry entry : feed.getEntries()) {
                    Map<String, String> map = EntryUtils.entryToMap(entry);
                    waitIfPause();
                    sourceEventListener.onEvent(map, null);
                }
            } else if (type.equals(Constants.RSS)) {
                Document<Feed> doc = abdera.getNewParser().parse(urlConnection.getInputStream(), url.toString());
                OMElement item = (OMElement) doc.getRoot();
                Iterator itemValue = item.getFirstElement().getChildrenWithName(Constants.FEED_ITEM);
                while (itemValue.hasNext()) {
                    Map<String, String> map = new HashMap<>();
                    OMElement omElement = (OMElement) itemValue.next();

                    Iterator titleValue = omElement.getChildrenWithName(Constants.FEED_TITLE);
                    OMElement title = (OMElement) titleValue.next();
                    map.put(Constants.TITLE, title.getText());

                    Iterator dateValue = omElement.getChildrenWithName(Constants.FEED_PUBDATE);
                    OMElement pubdate = (OMElement) dateValue.next();
                    map.put(Constants.PUBDATE, pubdate.getText());

                    Iterator idValue = omElement.getChildrenWithName(Constants.FEED_GUID);
                    OMElement guid = (OMElement) idValue.next();
                    map.put(Constants.ID, guid.getText());

                    Iterator linkValue = omElement.getChildrenWithName(Constants.FEED_LINK);
                    OMElement link = (OMElement) linkValue.next();
                    map.put(Constants.LINK, link.getText());
                    waitIfPause();
                    sourceEventListener.onEvent(map, null);
                }
            }
        } catch (IOException e) {
            log.error(" Connection Error in " + sourceEventListener.getStreamDefinition().getId() + " ", e);
        }
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
        try {
            lock.lock();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void waitIfPause() {
        if (paused) {
            lock.lock();
            try {
                while (paused) {
                    condition.await();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }
}
