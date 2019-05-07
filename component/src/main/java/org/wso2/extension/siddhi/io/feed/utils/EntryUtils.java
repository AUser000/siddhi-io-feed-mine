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

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility static methods for entry operations
 */
public class EntryUtils {
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
