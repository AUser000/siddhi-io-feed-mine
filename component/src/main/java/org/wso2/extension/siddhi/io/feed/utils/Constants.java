package org.wso2.extension.siddhi.io.feed.utils;

import javax.xml.namespace.QName;

/**
 * This is a sample class-level comment, explaining what the extension class does.
 */
public class Constants {

    public static final String HTTP_CREATED = "201";
    public static final String HTTP_OK = "201";
    public static final String HTTP_RESPONSE_CODE = "http.response.code";
    public static final String DEFAULT_REQUEST_INTERVAL = "20";

    private Constants() {}

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String CREDENTIALS = "admin";


    public static final String ATOM_FUNC = "atom.func";
    public static final String FEED_UPDATE = "update";
    public static final String FEED_CREATE = "create";
    public static final String FEED_DELETE = "delete";

    public static final String ATOM = "atom";
    public static final String RSS = "rss";

    public static final String URL = "url";
    public static final String REQUEST_INTERVAL = "request.interval";
    public static final String FEED_TYPE = "feed.type";


    public static final String ITEM = "item";
    //private static final String RSS = "rss";
    private static final String CHANNEL = "channel";
    private static final String TITLE = "title";
    private static final String GUID = "guid";
    private static final String PUBDATE = "pubDate";
    private static final String LINK = "link";


    public static final QName FEED_RSS = new QName(RSS);
    public static final QName FEED_CHANNEL = new QName(CHANNEL);
    public static final QName FEED_ITEM = new QName(ITEM);
    public static final QName FEED_TITLE = new QName(TITLE);
    public static final QName FEED_GUID = new QName(GUID);
    public static final QName FEED_PUBDATE = new QName(PUBDATE);
    public static final QName FEED_LINK = new QName(LINK);

    public static final String RSS_FEED_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
}
