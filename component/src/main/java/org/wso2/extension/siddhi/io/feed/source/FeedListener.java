package org.wso2.extension.siddhi.io.feed.source;


import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.feed.utils.Constants;
import org.wso2.extension.siddhi.io.feed.utils.EntryUtils;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
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
    private String streamName;

    public FeedListener(SourceEventListener sourceEventListener, URL url,
                        String type, String streamName) throws IOException {
        this.sourceEventListener = sourceEventListener;
        this.url = url;
        abdera = new Abdera();
        this.type = type;
        this.streamName = streamName;
    }

    @Override
    public void run() {
        try {
            URLConnection urlConnection = url.openConnection();
            Document<Feed> doc = abdera.getParser().parse(urlConnection.getInputStream(), url.toString());
            if (type.equals(Constants.ATOM)) {
                Feed feed = doc.getRoot();
                for (Entry entry : feed.getEntries()) {
                    Map<String, String> map = EntryUtils.entryToMap(entry);
                    waitIfPause();
                    sourceEventListener.onEvent(map, null);
                }
            } else if (type.equals(Constants.RSS)) {
                Factory factory = Abdera.getNewFactory();
                Feed feed = factory.newFeed();
                LinkedList<Entry> list = EntryUtils.convertRss(feed, doc);
                for (Entry entry : list) {
                    //TODO fix this double loop
                    Map<String, String> map = EntryUtils.entryToMap(entry);
                    waitIfPause();
                    sourceEventListener.onEvent(map, null);
                }
            }
        } catch (IOException e) {
            log.error(" Connection Error in " + sourceEventListener.getStreamDefinition().getId() , e);
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
