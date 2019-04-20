package org.wso2.extension.siddhi.io.feed.source;


import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.log4j.Logger;
import org.wso2.extension.siddhi.io.feed.utils.BasicAuthProperties;
import org.wso2.extension.siddhi.io.feed.utils.Constants;
import org.wso2.extension.siddhi.io.feed.utils.EntryUtils;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FeedListener implements Runnable {
    Logger log = Logger.getLogger(FeedSource.class);
    private boolean paused = false;
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();
    private SourceEventListener sourceEventListener;
    //private RSSFeed feed;
    private URL url;
    //private XmlReader reader;
    private Abdera abdera;
    private String type;
    private BasicAuthProperties basicAuthProperties;

    public FeedListener(SourceEventListener sourceEventListener, URL url, String type, BasicAuthProperties authProperty) throws IOException {
        this.sourceEventListener = sourceEventListener;
        this.url = url;
        abdera = new Abdera();

        this.type = type;
        basicAuthProperties = authProperty;
        URLConnection urlConnection;
    }

    @Override
    public void run() {
        try {
            Map<String, String> map = new HashMap<>();
            URLConnection urlConnection = url.openConnection();

            Document<Feed> doc = abdera.getParser().parse(urlConnection.getInputStream(), url.toString());
            log.info("got the log");
            if(type.equals(Constants.ATOM) || !type.equals(Constants.ATOM)) {
                Feed feed = doc.getRoot();
                log.info("get root details");
                // Get the entry items...
                if(feed.getEntries() != null) {
                    log.info("this is the reason");
                }
                for (Entry entry : feed.getEntries()) {
                    //log.info(entry.toString());
                    map = EntryUtils.entryToMap(entry);
                    //map.put("id", entry.getId().toString());
                    //map.put("title", entry.getTitle());
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
                    //map.put()
                    sourceEventListener.onEvent(map, null);
                }
                log.info("executing correct one");
            } else if (type.equals(Constants.RSS)) {
                log.info("executing wrong one");
            }
        } catch (IOException e) {
            System.out.println("Error : " + e);
        } catch (Exception ex) {
            System.out.println("Error : " + ex);
        } finally {
            // ToDO try to close connection!
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
}
