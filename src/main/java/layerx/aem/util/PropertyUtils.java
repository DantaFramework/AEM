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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import layerx.core.util.ObjectUtils;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import java.util.*;

import static layerx.Constants.RESERVED_SYSTEM_NAME_PREFIXES;

/**
 * Property utility class, contained generic methods for handling Node Property.
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2014-03-03
 */
public class PropertyUtils {

    /**
     * Extract property based on their type and return as object (of Property)
     *
     * @param property This is a property
     * @return content This is node property object
     * @throws Exception
     */
    public static Object distill(Property property)
            throws Exception {
        Object content;
        boolean isMulti = property.isMultiple();
        switch (property.getType()) {
            case PropertyType.LONG:
                if (isMulti)
                    content = asLongs(property);
                else
                    content = property.getLong();
                break;
            case PropertyType.DECIMAL:
            case PropertyType.DOUBLE:
                if (isMulti)
                    content = asDoubles(property);
                else
                    content = property.getDouble();
                break;
            case PropertyType.BOOLEAN:
                if (isMulti)
                    content = asBooleans(property);
                else
                    content = property.getBoolean();
                break;
            case PropertyType.DATE:
                if (isMulti)
                    content = asDates(property);
                else
                    content = property.getValue().getString();
                break;
            case PropertyType.NAME:
            case PropertyType.STRING:
            case PropertyType.PATH:
            case PropertyType.URI:
            default:
                if (isMulti)
                    content = asStrings(property);
                else
                    content = property.getString();
                break;
        }
        return content;
    }

    /**
     * Convert property to map
     *
     * @param map This is a map of Objects
     * @return map This is a map of Objects of Property
     * @throws Exception
     */
    public static Map<String, Object> propsToMap(Map map)
            throws Exception {
        return propsToMap(map, true);
    }

    /**
     * Takes a list of Objects of Property and turns it into map.
     *
     * @param map This is a map of Objects of Property
     * @param ignoreSystemNames This is a boolean value to whether ignore system names
     * @return map This is a list of Objects of Property
     * @throws Exception
     */
    public static Map<String, Object> propsToMap(Map map, final boolean ignoreSystemNames)
            throws Exception {
        return ObjectUtils.wrap(Maps.<String, Object>filterKeys(map, Predicates.not(new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return ignoreSystemNames && StringUtils.startsWithAny(input, RESERVED_SYSTEM_NAME_PREFIXES);
            }
        })));
    }

    /**
     * Takes an Iterator properties and turns it into map.
     *
     * @param properties This is an Iterator of Properties
     * @return map This is a list of Objects of Property
     * @throws Exception
     */
    public static Map<String, Object> propsToMap(Iterator properties)
            throws Exception {
        return propsToMap(properties, true);
    }

    /**
     * Takes an Iterator properties and turns it into map.
     *
     * @param properties This is a list of Iterator Properties
     * @param ignoreSystemNames This is a boolean value to whether ignore system names
     * @return content This is a list of Objects of Property
     * @throws Exception
     */
    public static Map<String, Object> propsToMap(Iterator properties, boolean ignoreSystemNames)
            throws Exception {
        Map<String, Object> content = new JSONObject();
        while (properties.hasNext()) {
            Property property = (Property) properties.next(); // This is required so we can take any kind of Iterator, not just PropertyIterator
            String propertyName = property.getName();
            if (ignoreSystemNames && StringUtils.startsWithAny(propertyName, RESERVED_SYSTEM_NAME_PREFIXES))
                continue;
            content.put(property.getName(), distill(property));
        }
        return content;
    }

    /**
     * Takes a Collection of Property and turns it into map.
     *
     * @param properties This is a Collection of Property
     * @return map
     * @throws Exception
     */
    public static Map<String, Object> propsToMap(Collection<Property> properties)
            throws Exception {
        return propsToMap(properties.iterator());
    }

    /**
     * Takes any list of Property and turns it into map.
     *
     * @param properties This is a Collection of Property
     * @return map
     * @throws Exception
     */
    public static Map<String, Object> propsToMap(Property... properties)
            throws Exception {
        return propsToMap(Arrays.asList(properties));
    }

    /**
     * Takes a Resource and turns it into map.
     *
     * @param resource This is a Resource
     * @return map
     * @throws Exception
     */
    public static Map<String, Object> propsToMap(Resource resource)
            throws Exception {
        return propsToMap(resource.adaptTo(Node.class));
    }

    /**
     * Takes a Node and turns it into map.
     *
     * @param node This is a Node
     * @return map
     * @throws Exception
     */
    public static Map<String, Object> propsToMap(Node node)
            throws Exception {
        return propsToMap(node.getProperties());
    }

    /**
     * Takes a Property and casts it into a Collection of String.
     *
     * @param property This is a Property
     * @return collection This is a Collection of String
     * @throws Exception
     */
    public static Collection<String> asStrings(Property property)
            throws Exception {
        return as(String.class, property);
    }

    /**
     * Takes a Property and casts it into a Collection of Long.
     *
     * @param property This is a Property
     * @return collection This is a Collection of Long
     * @throws Exception
     */
    public static Collection<Long> asLongs(Property property)
            throws Exception {
        return as(Long.class, property);
    }

    /**
     * Takes a Property and casts it into a Collection of Double.
     *
     * @param property This is a Property
     * @return collection This is a Collection of Double
     * @throws Exception
     */
    public static Collection<Double> asDoubles(Property property)
            throws Exception {
        return as(Double.class, property);
    }

    /**
     * Takes a Property and casts it into a Collection of Boolean.
     *
     * @param property This is a Property
     * @return collection This is a Collection of Boolean
     * @throws Exception
     */
    public static Collection<Boolean> asBooleans(Property property)
            throws Exception {
        return as(Boolean.class, property);
    }

    /**
     * Takes a Property and casts it into a Collection of Date.
     *
     * @param property This is a Property
     * @return collection This is a Collection of Date
     * @throws Exception
     */
    public static Collection<Date> asDates(Property property)
            throws Exception {
        return as(Date.class, property);
    }

    /**
     * Takes a generic type and a Property and turns the property into a List of Objects of that given type.
     *
     * @param type This is a type parameter
     * @param property This is a Property
     * @return list This is a List of Objects of the given type
     * @throws Exception
     */
    public static <T> List<T> as(Class<T> type, Property property)
            throws Exception {
        return as(type, property.getValues());
    }

    /**
     * Takes a generic type and a list of Values and turns the values into a List of Objects of that given type.
     *
     * @param type This is a type parameter
     * @param values This is a list of Values
     * @return list This is a List of Objects of the given type
     * @throws Exception
     */
    public static <T> List<T> as(Class<T> type, Value... values)
            throws Exception {
        return as(type, Arrays.asList(values));
    }

    /**
     * Takes a generic type and a Collection of Values and turns the Collection of Values into a List of Objects of that given type.
     *
     * @param type This is a type parameter
     * @param values This is a Collection of Values
     * @return list of values of the given type
     * @throws Exception
     */
    public static <T> List<T> as(Class<T> type, Collection<Value> values)
            throws Exception {
        List<T> distilledValues = new ArrayList<>();
        for (Value value : values) {
            distilledValues.add(as(type, value));
        }
        return distilledValues;
    }

    /**
     * Takes a generic type and a Value and turns the Value into the Object of that given type.
     *
     * @param type This is a type parameter
     * @param value This is the value
     * @return castedValue This is the value of the given type
     * @throws Exception
     */
    public static <T> T as(Class<T> type, Value value)
            throws Exception {
        if (type == Long.class)
            return (T) new Long(value.getLong());
        if (type == Double.class)
            return (T) new Double(value.getDouble());
        if (type == Boolean.class)
            return (T) new Boolean(value.getBoolean());
        if (type == String.class)
            return (T) value.getString();
        if (type == Date.class)
            return (T) value.getString();
        if (type == Calendar.class)
            return (T) value.getDate();

        return null;
    }
}
