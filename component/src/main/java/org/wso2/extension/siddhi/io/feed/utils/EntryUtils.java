package org.wso2.extension.siddhi.io.feed.utils;

import org.apache.abdera.Abdera;
import org.apache.abdera.factory.Factory;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.axiom.om.OMElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class EntryUtils {
    public static Entry createEntry(HashMap<String, String> map, Factory factory) {
        Entry entry = factory.newEntry();
        entry.setId(map.get("id"));
        if(map.get("title") != null) {
            entry.setTitle(map.get("title"));
        }
        if(map.get("updated") != null) {
            entry.setUpdated(map.get("updated"));
        }
        if(map.get("content") != null) {
            entry.setContent(map.get("content"));
        }
        entry.setUpdated(new Date());
        return entry;
    }

    public static LinkedList<Entry> convertRss (Feed feed, Document doc) {
        Factory factory = Abdera.getNewFactory();
        feed = factory.newFeed();
        OMElement item = (OMElement) doc.getRoot();
        Iterator itemValue = item.getFirstElement().getChildrenWithName(Constants.FEED_ITEM);
        LinkedList<Entry> list = new LinkedList<>();
        while (itemValue.hasNext()) {
            Entry entry = feed.insertEntry();
            OMElement omElement = (OMElement) itemValue.next();

            Iterator titleValue = omElement.getChildrenWithName(Constants.FEED_TITLE);
            OMElement Title = (OMElement) titleValue.next();
            entry.setTitle(Title.getText());

            Iterator dateValue = omElement.getChildrenWithName(Constants.FEED_PUBDATE);
            OMElement Updated = (OMElement) dateValue.next();
            //Date date;
            entry.setUpdated(Updated.getText());

            Iterator idValue = omElement.getChildrenWithName(Constants.FEED_GUID);
            OMElement guid = (OMElement) idValue.next();
            entry.setId(guid.getText());

            Iterator linkValue = omElement.getChildrenWithName(Constants.FEED_LINK);
            OMElement link = (OMElement) linkValue.next();
            entry.setBaseUri(link.getText());
        }
        return list;
    }

    // <entry xmlns="http://www.w3.org/2005/Atom">
    //  <link href="/news/1001-name" rel="edit"/>
    //  <id>urn:acme:customer:1001</id>
    //  <title type="text">name</title>
    //  <updated>2019-04-19T13:52:51.558Z</updated>
    //  <author><name>Acme Industries</name></author>
    //  <content type="text">name</content>
    // </entry>
    public static Map<String, String> entryToMap(Entry entry) {
        Map<String, String> map = new HashMap<>();

        map.put("id", entry.getId().toString());
        map.put("title", entry.getTitle());
        if(entry.getLinks() != null) {
            ArrayList<String> list = new ArrayList<>();
            for (Link l:entry.getLinks()) {
                list.add(l.getHref().toString());
            }
            map.put("link", String.join(",", list));
        }
        if(entry.getUpdated() != null) {
            map.put("updated", entry.getUpdated().toString());
        }
        if(entry.getAuthor() != null) {
            map.put("author", entry.getAuthor().toString());
        }
        if(entry.getPublished() != null) {
            map.put("published", entry.getPublished().toString());
        }
        return map;
    }
}
