/**
 * Danta AEM Bundle
 *
 * Copyright (C) 2017 Tikal Technologies, Inc. All rights reserved.
 *
 * Licensed under GNU Affero General Public License, Version v3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied;
 * without even the implied warranty of MERCHANTABILITY.
 * See the License for more details.
 */

package danta.aem.util;

import net.minidev.json.JSONObject;
import org.apache.sling.api.request.RecursionTooDeepException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Container for the CQ general objects.
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-03-13
 */
public class ResourceTraversor {

    /**
     * Inner class Entry
     */
    public static final class Entry {
        public final Resource resource;
        public final JSONObject json;

        /**
         * Constructor
         *
         * @param r The resource
         * @param o The JSONObject
         */
        public Entry(final Resource r, final JSONObject o) {
            this.resource = r;
            this.json = o;
        }
    }

    private long count;

    private long maxResources;

    private final int maxRecursionLevels;

    private final JSONObject startObject;

    private LinkedList<Entry> currentQueue;

    private LinkedList<Entry> nextQueue;

    private final Resource startResource;

    /**
     * Constructor
     *
     * @param resource The resource to be traversed through
     * @param levels The levels to maximum levels to be executed
     * @param maxResources The maximum level of resource
     * @throws Exception
     */
    public ResourceTraversor(final Resource resource, final int levels, final long maxResources)
            throws Exception {
        this.maxResources = maxResources;
        this.maxRecursionLevels = levels;
        this.startResource = resource;
        currentQueue = new LinkedList<Entry>();
        nextQueue = new LinkedList<Entry>();
        this.startObject = this.adapt(resource);
    }

    /**
     * Recursive descent from startResource, collecting JSONObjects into
     * startObject. Throws a RecursionTooDeepException if the maximum number of
     * nodes is reached on a "deep" traversal (where "deep" === level greater
     * than 1).
     *
     * @return -1 if everything went fine, a positive valuew when the resource
     * has more child nodes then allowed.
     * @throws Exception
     */
    public int collectResources()
            throws RecursionTooDeepException, Exception {
        return collectChildren(startResource, this.startObject, 0);
    }

    /**
     * @param resource
     * @param currentLevel
     * @throws Exception
     */
    private int collectChildren(final Resource resource, final JSONObject jsonObj, int currentLevel)
            throws Exception {

        if (maxRecursionLevels == -1 || currentLevel < maxRecursionLevels) {
            final Iterator<Resource> children = ResourceUtil.listChildren(resource);
            while (children.hasNext()) {
                count++;
                final Resource res = children.next();
                // SLING-2320: always allow enumeration of one's children;
                // DOS-limitation is for deeper traversals.
                if (count > maxResources && maxRecursionLevels != 1) {
                    return currentLevel;
                }
                final JSONObject json = collectResource(res, jsonObj);
                nextQueue.addLast(new Entry(res, json));
            }
        }

        while (!currentQueue.isEmpty() || !nextQueue.isEmpty()) {
            if (currentQueue.isEmpty()) {
                currentLevel++;
                currentQueue = nextQueue;
                nextQueue = new LinkedList<Entry>();
            }
            final Entry nextResource = currentQueue.removeFirst();
            final int maxLevel = collectChildren(nextResource.resource, nextResource.json, currentLevel);
            if (maxLevel != -1) {
                return maxLevel;
            }
        }
        return -1;
    }

    /**
     * Adds a resource in the JSON tree.
     *
     * @param resource The resource to add
     * @throws Exception
     */
    private JSONObject collectResource(Resource resource, final JSONObject parent)
            throws Exception {
        final JSONObject o = adapt(resource);
        parent.put(ResourceUtil.getName(resource), o);
        return o;
    }

    /**
     * Adapt a Resource to a JSON Object.
     *
     * @param resource The resource to adapt.
     * @return The JSON representation of the Resource
     * @throws Exception
     */
    private JSONObject adapt(final Resource resource)
            throws Exception {
        return ResourceToJSONSerializer.create(resource, 0);
    }

    /**
     * @return The number of resources this visitor found.
     */
    public long getCount() {
        return count;
    }

    /**
     *
     * @return jsonObject
     */
    public JSONObject getJSONObject() {
        return startObject;
    }
}
