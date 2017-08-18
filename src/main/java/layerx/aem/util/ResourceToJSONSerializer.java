/**
 * LayerX AEM Bundle
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

package layerx.aem.util;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Container for the CQ general objects.
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-03-13
 */
public abstract class ResourceToJSONSerializer {

    /**
     * Dump given resource in JSON, optionally recursing into its objects
     *
     * @param resource This is the resource to be used to create JSONObject
     * @param maxRecursionLevels This is the level of recursion that needs to be executed
     * @return jsonObject
     */
    public static JSONObject create(final Resource resource, final int maxRecursionLevels)
            throws Exception {
        return create(resource, 0, maxRecursionLevels);
    }

    /**
     * Dump given resource in JSON, optionally recursing into its objects
     *
     * @param resource This is the resource to be used to create JSONObject
     * @param currentRecursionLevel This is the current level of recursion
     * @param maxRecursionLevels This is the level of recursion that needs to be executed
     * @return jsonObject
     */
    private static JSONObject create(final Resource resource,
                                     final int currentRecursionLevel,
                                     final int maxRecursionLevels)
            throws Exception {
        final ValueMap valueMap = resource.adaptTo(ValueMap.class);

        @SuppressWarnings("unchecked")
        final Map propertyMap = (valueMap != null)
                ? valueMap
                : resource.adaptTo(Map.class);

        final JSONObject obj = new JSONObject();


        if (propertyMap == null) {

            // no map available, try string
            final String value = resource.adaptTo(String.class);
            if (value != null) {

                // single value property or just plain String resource or...
                obj.put(ResourceUtil.getName(resource), value);

            } else {

                // Try multi-value "property"
                final String[] values = resource.adaptTo(String[].class);
                if (values != null) {
                    obj.put(ResourceUtil.getName(resource), new JSONArray().addAll(Arrays.asList(values)));
                }

            }
            if (resource.getResourceType() != null) {
                obj.put("sling:resourceType", resource.getResourceType());
            }
            if (resource.getResourceSuperType() != null) {
                obj.put("sling:resourceSuperType", resource.getResourceSuperType());
            }

        } else {

            @SuppressWarnings("unchecked")
            final Iterator<Map.Entry> props = propertyMap.entrySet().iterator();

            // the node's actual properties
            while (props.hasNext()) {
                @SuppressWarnings("unchecked")
                final Map.Entry prop = props.next();

                if (prop.getValue() != null) {
                    createProperty(obj, valueMap, prop.getKey().toString(),
                            prop.getValue());
                }
            }
        }

        // the child nodes
        if (recursionLevelActive(currentRecursionLevel, maxRecursionLevels)) {
            final Iterator<Resource> children = ResourceUtil.listChildren(resource);
            while (children.hasNext()) {
                final Resource n = children.next();
                createSingleResource(n, obj, currentRecursionLevel,
                        maxRecursionLevels);
            }
        }

        return obj;
    }

    /**
     * Used to format date values
     */
    private static final String ECMA_DATE_FORMAT = "EEE MMM dd yyyy HH:mm:ss 'GMT'Z";

    /**
     * Used to format date values
     */
    private static final Locale DATE_FORMAT_LOCALE = Locale.US;

    private static final DateFormat CALENDAR_FORMAT = new SimpleDateFormat(ECMA_DATE_FORMAT, DATE_FORMAT_LOCALE);

    private static synchronized String format(final Calendar date) {
        return CALENDAR_FORMAT.format(date.getTime());
    }

    /**
     * Dump only a value in the correct format
     *
     * @param value This is an object to be dumped
     * @return value
     */
    private static Object getValue(final Object value) {
        if (value instanceof InputStream) {
            // input stream is already handled
            return 0;
        } else if (value instanceof Calendar) {
            return format((Calendar) value);
        } else if (value instanceof Boolean) {
            return value;
        } else if (value instanceof Long) {
            return value;
        } else if (value instanceof Integer) {
            return value;
        } else if (value instanceof Double) {
            return value;
        } else {
            return value.toString();
        }
    }

    /**
     * Dump a single node
     *
     * @param n This is the resource to be used to create JSONObject
     * @param parent This is the parent to be used to create the JSONObject from the Resource n under
     * @param currentRecursionLevel This is the current level of recursion
     * @param maxRecursionLevels This is the maximum recursion level
     */
    private static void createSingleResource(final Resource n, final JSONObject parent,
                                             final int currentRecursionLevel, final int maxRecursionLevels)
            throws Exception {
        if (recursionLevelActive(currentRecursionLevel, maxRecursionLevels)) {
            parent.put(ResourceUtil.getName(n), create(n, currentRecursionLevel + 1, maxRecursionLevels));
        }
    }

    /**
     * true if the current recursion level is active
     *
     * @param currentRecursionLevel This is the current level of recursion
     * @param maxRecursionLevels This is the maximum recursion level
     */
    private static boolean recursionLevelActive(final int currentRecursionLevel,
                                                final int maxRecursionLevels) {
        return maxRecursionLevels < 0
                || currentRecursionLevel < maxRecursionLevels;
    }

    /**
     * Write a single property
     *
     * @param obj This is the JSONObject to be used for creating Property
     * @param valueMap This is the ValueMap to be used to store the Property in
     * @param key This is the key to be used for storing Object
     * @param value This is the value to be stored
     */
    private static void createProperty(final JSONObject obj, final ValueMap valueMap, final String key, final Object value)
            throws Exception {
        Object[] values = null;
        if (value.getClass().isArray()) {
            values = (Object[]) value;
            // write out empty array
            if (values.length == 0) {
                obj.put(key, new JSONArray());
                return;
            }
        }

        // special handling for binaries: we dump the length and not the data!
        if (value instanceof InputStream || (values != null && values[0] instanceof InputStream)) {
            // TODO for now we mark binary properties with an initial colon in
            // their name
            // (colon is not allowed as a JCR property name)
            // in the name, and the value should be the size of the binary data
            if (values == null) {
                obj.put(":" + key, getLength(valueMap, -1, key, (InputStream) value));
            } else {
                final JSONArray result = new JSONArray();
                for (int i = 0; i < values.length; i++) {
                    result.add(getLength(valueMap, i, key, (InputStream) values[i]));
                }
                obj.put(":" + key, result);
            }
            return;
        }

        if (!value.getClass().isArray()) {
            obj.put(key, getValue(value));
        } else {
            final JSONArray result = new JSONArray();
            for (Object v : values) {
                result.add(getValue(v));
            }
            obj.put(key, result);
        }
    }

    /**
     * Get length from an object in a ValueMap
     *
     * @param valueMap This is the ValueMap to fetch the object from
     * @param index This is the position of where the length is
     * @param key This is the position of where the object is in the ValueMap
     * @param stream This is the InputStream to be closed (if connection is still alive)
     * @return length
     */
    private static long getLength(final ValueMap valueMap, final int index, final String key, final InputStream stream) {
        try {
            stream.close();
        } catch (IOException ignore) {
        }
        long length = -1;
        if (valueMap != null) {
            if (index == -1) {
                length = valueMap.get(key, length);
            } else {
                Long[] lengths = valueMap.get(key, Long[].class);
                if (lengths != null && lengths.length > index) {
                    length = lengths[index];
                }
            }
        }
        return length;
    }
}
