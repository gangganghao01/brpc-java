/*
 * Copyright (c) 2018 Baidu, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baidu.brpc.naming;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * format is "list://127.0.0.1:8002,127.0.0.1:8003/path?key1=value1&key2=value2"
 */
@Setter
@Getter
@Slf4j
public class BrpcURI {
    public static final String GROUP = "group";
    public static final String VERSION = "version";
    // update timer interval for pull mode
    public static final String INTERVAL = "interval";
    public static final int DEFAULT_INTERVAL = 1000;

    private String schema;
    private List<String> hosts = new ArrayList<String>();
    private List<String> ports = new ArrayList<String>();
    private String path;
    private Map<String, Object> queryMap = new HashMap<String, Object>();

    public BrpcURI(String uri) {
        // schema
        int index = uri.indexOf("://");
        if (index < 0) {
            throw new IllegalArgumentException("invalid uri:" + uri);
        }
        this.schema = uri.substring(0, index).toLowerCase();
        // host:port
        String hostPorts;
        int index2 = uri.indexOf('/', index + 3);
        int index3 = uri.indexOf('?', index + 3);
        if (index2 > 0) {
            hostPorts = uri.substring(index + 3, index2);
        } else if (index3 > 0) {
            hostPorts = uri.substring(index + 3, index3);
        } else {
            hostPorts = uri.substring(index + 3);
        }
        int len = hostPorts.length();
        if (len > 0) {
            String[] hostPortSplits = hostPorts.split(",");
            for (String hostPort : hostPortSplits) {
                String[] hostPortSplit = hostPort.split(":");
                String host = hostPortSplit[0];
                String port;
                if (hostPortSplit.length == 2) {
                    port = hostPortSplit[1];
                } else {
                    port = "";
                }
                this.hosts.add(host);
                this.ports.add(port);
            }
        }

        // path
        if (index2 > 0) {
            if (index3 > 0) {
                this.path = uri.substring(index2, index3);
            } else {
                this.path = uri.substring(index2);
            }
        } else {
            this.path = "/";
        }

        // query
        if (index3 > 0) {
            String query = uri.substring(index3 + 1);
            String[] querySplits = query.split("&");
            for (String kv : querySplits) {
                String[] kvSplit = kv.split("=");
                queryMap.put(kvSplit[0], kvSplit[1]);
            }
        }
    }

    public void addParameter(String key, Object value) {
        queryMap.put(key, value);
    }

    public Object getParameter(String key) {
        return queryMap.get(key);
    }

    public Object getParameter(String key, Object defaultValue) {
        Object value = queryMap.get(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    public int getIntParameter(String key, int defaultValue) {
        Object value = queryMap.get(key);
        if (value != null) {
            return Integer.valueOf((String) value);
        } else {
            return defaultValue;
        }
    }
}
