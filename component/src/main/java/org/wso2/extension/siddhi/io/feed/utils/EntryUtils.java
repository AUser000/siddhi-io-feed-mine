package org.wso2.extension.siddhi.io.feed.utils;

import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.axiom.om.OMElement;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

/**
 * This is a sample class-level comment, explaining what the extension class does.
 */
public class EntryUtils {
    public static LinkedList<Entry> convertRss (Feed feed, Document doc) {
        OMElement item = (OMElement) doc.getRoot();
        Iterator itemValue = item.getFirstElement().getChildrenWithName(Constants.FEED_ITEM);
        LinkedList<Entry> list = new LinkedList<>();
        DateFormat format = new SimpleDateFormat(Constants.RSS_FEED_DATE_FORMAT, Locale.ENGLISH);
        while (itemValue.hasNext()) {
            Entry entry = feed.insertEntry();
            OMElement omElement = (OMElement) itemValue.next();

            Iterator titleValue = omElement.getChildrenWithName(Constants.FEED_TITLE);
            OMElement title = (OMElement) titleValue.next();
            entry.setTitle(title.getText());

            Iterator dateValue = omElement.getChildrenWithName(Constants.FEED_PUBDATE);
            OMElement updated = (OMElement) dateValue.next();
            try {
                Date date = format.parse(updated.getText());
                entry.setUpdated(date);
            } catch (ParseException e) {
                //log.error();
            }

            Iterator idValue = omElement.getChildrenWithName(Constants.FEED_GUID);
            OMElement guid = (OMElement) idValue.next();
            entry.setId(guid.getText());

            Iterator linkValue = omElement.getChildrenWithName(Constants.FEED_LINK);
            OMElement link = (OMElement) linkValue.next();
            entry.setBaseUri(link.getText());

            list.add(entry);
        }
        return list;
    }

    public static Map<String, String> entryToMap(Entry entry) {
        Map<String, String> map = new HashMap<>();

        map.put("id", entry.getId().toString());
        map.put("title", entry.getTitle());
        if (entry.getLinks() != null) {
            ArrayList<String> list = new ArrayList<>();
            for (Link l:entry.getLinks()) {
                list.add(l.getHref().toString());
            }
            map.put("link", String.join(",", list));
        }
        if (entry.getUpdated() != null) {
            map.put("updated", entry.getUpdated().toString());
        }
        if (entry.getAuthor() != null) {
            map.put("author", entry.getAuthor().toString());
        }
        if (entry.getPublished() != null) {
            map.put("published", entry.getPublished().toString());
        }
        return map;
    }

    public static Entry createEntry(HashMap<String, String> map, Entry entry) {
        for (String key: map.keySet()) {
            switchReturn(key, entry, map);
        }
        entry.setUpdated(new Date());
        return entry;
    }

    private static Entry switchReturn(String key, Entry entry, Map<String, String> map) {
        switch (key) {
            case "id" :
                entry.setId(map.get("id"));
                return entry;
            case "title" :
                entry.setTitle(map.get("title"));
                return entry;
            case "content" :
                entry.setContent(map.get("content"));
                return entry;
            case "link" :
                entry.addLink(map.get("link"));
                return entry;
            case "author" :
                entry.addAuthor(map.get("author"));
                return entry;
            default:
                return entry;
        }
    }

}
