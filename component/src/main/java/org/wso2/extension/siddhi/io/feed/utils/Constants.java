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

package org.wso2.extension.siddhi.io.feed.utils;

import javax.xml.namespace.QName;

/**
 * Constant Values
 */
public class Constants {
    private Constants() {}

    public static final String HTTP_CREATED = "201";
    public static final String HTTP_OK = "200";
    public static final String HTTP_RESPONSE_CODE = "http.response.code";
    public static final String DEFAULT_REQUEST_INTERVAL = "20";


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

    public static final String ID = "id";
    public static final String ITEM = "item";
    //public static final String CHANNEL = "channel";
    public static final String TITLE = "title";
    public static final String GUID = "guid";
    public static final String PUBDATE = "pubDate";
    //public static final String UPDATED = "updated";
    public static final String LINK = "link";


    //public static final QName FEED_RSS = new QName(RSS);
    //public static final QName FEED_CHANNEL = new QName(CHANNEL);
    public static final QName FEED_ITEM = new QName(ITEM);
    public static final QName FEED_TITLE = new QName(TITLE);
    public static final QName FEED_GUID = new QName(GUID);
    public static final QName FEED_PUBDATE = new QName(PUBDATE);
    public static final QName FEED_LINK = new QName(LINK);

    //public static final String RSS_FEED_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
}
