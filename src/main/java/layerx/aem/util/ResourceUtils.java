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

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.components.ComponentManager;
import com.day.cq.wcm.api.designer.Design;
import com.day.cq.wcm.api.designer.Designer;
import com.day.cq.wcm.api.designer.Style;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import static com.day.cq.wcm.api.NameConstants.NT_COMPONENT;
import static layerx.Constants.*;
import static layerx.aem.Constants.APPS_ROOT;
import static layerx.aem.Constants.GLOBAL_PATH;

/**
 * Resource utility class.
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-03-13
 */
public class ResourceUtils {

    protected static final Logger log = LoggerFactory.getLogger(ResourceUtil.class);

    /**
     * Forbid instantiation
     */
    private ResourceUtils() {
    }

    /**
     * Takes resource and turns it into JSONObject
     *
     * @param resource The resource to turn into JSONObject
     * @return jsonObject
     * @throws Exception
     */
    public static JSONObject toJSON(Resource resource)
            throws Exception {
        return toJSON(resource, -1);
    }

    /**
     * Takes resource and turns it into JSONObject, stops execution at a given levels
     *
     * @param resource The resource to turn into JSONObject
     * @param levels The maximum level to execute
     * @return jsonObject
     * @throws Exception
     */
    public static JSONObject toJSON(final Resource resource, final int levels)
            throws Exception {
        return toJSON(resource, levels, Integer.MAX_VALUE);
    }

    /**
     * Takes resource and turns it into JSONObject, stops execution at a given levels & maximum number of resources
     *
     * @param resource The resource to turn into JSONObject
     * @param levels The maximum level to execute
     * @param maxResources The maximum number of resources
     * @return jsonObject
     * @throws Exception
     */
    public static JSONObject toJSON(final Resource resource, final int levels, final long maxResources)
            throws Exception {
        ResourceTraversor traversor = new ResourceTraversor(resource, levels, maxResources);
        return traversor.getJSONObject();
    }

    /**
     * Takes resource and turns it into String. If readable is true, returns readable string; else returns compressed
     *
     * @param resource The resource to turn into JSONObject
     * @param readable The flag to determine whether the string should be compressed or not
     * @return string Readable or compressed
     * @throws Exception
     */
    public static String toJSONString(final Resource resource, final boolean readable)
            throws Exception {
        return toJSONString(resource, -1, Integer.MAX_VALUE, readable);
    }

    /**
     * Takes resource and turns it into String. If readable is true, returns readable string; else returns compressed
     *
     * @param resource The resource to turn into JSONObject
     * @param levels The levels to be iterated through
     * @param readable The flag to determine whether the string should be compressed or not
     * @return string Readable or compressed
     * @throws Exception
     */
    public static String toJSONString(final Resource resource, final int levels, final long maxResources, final boolean readable)
            throws Exception {
        JSONObject resourceObj = toJSON(resource, levels, maxResources);
        return (readable) ? resourceObj.toJSONString(JSONStyle.NO_COMPRESS) : resourceObj.toJSONString(JSONStyle.MAX_COMPRESS);
    }

    /**
     * Takes resource and return the app name of the resource
     *
     * @param resource The resource to get the name from
     * @return appName
     */
    public static String getAppName(Resource resource) {
        String resourceType = resource.getResourceType();
        resourceType = resourceType.startsWith(APPS_ROOT) ? resourceType.replace(APPS_ROOT, "") : resourceType;
        return resourceType.substring(0, resourceType.indexOf("/"));
    }

    /**
     * Takes resource and resource resolver return the global property path from the resource
     *
     * @param resource The resource to get the global property path from
     * @return globalPropertiesPath
     */
    public static String getGlobalPropertiesPath(Resource resource, ResourceResolver resourceResolver)
            throws RepositoryException, PersistenceException {
        String globalPropertiesPath = "";
        Designer designer = resourceResolver.adaptTo(Designer.class);
        Style style = designer.getStyle(resource);
        Design design;
        if (null != style) {
            design = style.getDesign();
            if (null != design) {
                if (null != design.getContentResource()) {
                    if (null != design.getContentResource().getPath()) {
                        //add global node in design when it does not exist
                        Resource designResource = resourceResolver.getResource(design.getContentResource().getPath());
                        Node designNode = designResource.adaptTo(Node.class);
                        if (!designNode.hasNode(GLOBAL_PROPERTIES_KEY)) {
                            designNode.addNode(GLOBAL_PROPERTIES_KEY);
                            resourceResolver.commit();
                        }
                        // set global path
                        globalPropertiesPath = design.getContentResource().getPath() + GLOBAL_PATH;
                    }
                }
            }
        }
        return globalPropertiesPath;
    }

    /**
     * Takes resource and resource type return the parent resource based on the given type
     *
     * @param resource The resource to get the parent from
     * @return resource
     */
    public static Resource findParentAs(final Resource resource, final String resourceType) {
        try {
            if (resource != null) {
                Node parentNode = resource.adaptTo(Node.class);
                return (resourceType != null && !resourceType.isEmpty() && !ResourceUtil.isNonExistingResource(resource) && (resource.isResourceType(resourceType) || parentNode.getPrimaryNodeType().isNodeType(resourceType))) ? resource : findParentAs(resource.getParent(), resourceType);
            }
        } catch (Exception ew) {
            log.error(ERROR, ew);
        }
        return null;
    }

    /**
     * Takes resource and returns component
     *
     * @param resource The resource to fetch the component from
     * @return null or component
     */
    public static Component findContainingComponent(final Resource resource) {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        ComponentManager componentManager = resourceResolver.adaptTo(ComponentManager.class);
        Resource componentResource = findParentAs(resource, NT_COMPONENT);
        if (componentResource != null) {
            String componentPath = componentResource.getPath();
            if (StringUtils.isNotBlank(componentPath)) {
                return componentManager.getComponent(componentPath);
            }
        }
        return null;
    }

    /**
     * Takes resource and returns page
     *
     * @param resource The resource to adapt into page
     * @return page
     */
    public static Page findContainingPage(final Resource resource) {
        ResourceResolver resourceResolver = resource.getResourceResolver();
        final PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
        return pageManager.getContainingPage(resource);
    }

    /**
     * Takes type, resource, and propertyName and returns its value of the object based on the given type
     *
     * @param type This is type parameter
     * @param resource The resource to fetch the value from
     * @param propertyName The property name to be used to fetch the value from the resource
     * @return value The value of the object based on the given type
     */
    private static <T> T getSinglePropertyAs(Class<T> type, Resource resource, String propertyName) {
        T val = null;
        try {
            if (null != resource) {
                Node node = resource.adaptTo(Node.class);
                if (null != node) {
                    if (node.hasProperty(propertyName)) {
                        Property property = node.getProperty(propertyName);
                        if (!property.isMultiple()) {
                            Value value = property.getValue();
                            val = PropertyUtils.as(type, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(ERROR, e);
        }
        return val;
    }

    /**
     * Takes type, resource, and property name and returns the list of value of the object based on the given type
     *
     * @param type This is type parameter
     * @param resource The resource to fetch the value from
     * @param propertyName The property name to be used to fetch the value from the resource
     * @return valueList The list of values of the object based on the given type
     */
    private static <T> List<T> getMultiplePropertyAs(Class<T> type, Resource resource, String propertyName) {
        List<T> val = Collections.EMPTY_LIST;
        try {
            if (null != resource) {
                Node node = resource.adaptTo(Node.class);
                if (null != node) {
                    if (node.hasProperty(propertyName)) {
                        Property property = node.getProperty(propertyName);
                        if (property.isMultiple()) {
                            Value[] value = property.getValues();
                            val = PropertyUtils.as(type, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(ERROR, e);
        }
        return val;
    }

    /**
     * Takes resource and property name and return the value as string
     *
     * @param resource The resource to fetch property's value from
     * @param propertyName The property name
     * @return value
     */
    public static String getPropertyAsString(Resource resource, String propertyName) {
        String value = null;
        if (!isPropertyMultiple(resource, propertyName)) {
            value = getSinglePropertyAs(String.class, resource, propertyName);
        }
        return (null == value) ? BLANK : value;
    }

    /**
     * Takes resource and property name and return the value as list of string
     *
     * @param resource The resource to fetch property's value from
     * @param propertyName The property name
     * @return list This is a list of string
     */
    public static List<String> getPropertyAsStrings(Resource resource, String propertyName) {
        List<String> value;
        if (isPropertyMultiple(resource, propertyName)) {
            value = getMultiplePropertyAs(String.class, resource, propertyName);
        } else {
            value = new ArrayList<>();
            value.add(getSinglePropertyAs(String.class, resource, propertyName));
        }
        return value;
    }

    /**
     * Takes resource and property name and return the value as boolean
     *
     * @param resource The resource to fetch property's value from
     * @param propertyName The property name
     * @return value This is boolean
     */
    public static Boolean getPropertyAsBoolean(Resource resource, String propertyName) {
        Boolean value = false;
        if (!isPropertyMultiple(resource, propertyName)) {
            String stringValue = getSinglePropertyAs(String.class, resource, propertyName);
            if (StringUtils.isEmpty(stringValue)) {
                value = getSinglePropertyAs(Boolean.class, resource, propertyName);
                value = (value == null) ? false : value;
            } else {
                value = TRUE.equals(stringValue);
            }
        }
        return value;
    }

    /**
     * Takes resource and property name and return the value as calendar
     *
     * @param resource The resource to fetch property's value from
     * @param propertyName The property name
     * @return value This is calendar
     */
    public static Calendar getPropertyAsCalendar(Resource resource, String propertyName) {
        Calendar value = null;
        if (!isPropertyMultiple(resource, propertyName)) {
            value = getSinglePropertyAs(Calendar.class, resource, propertyName);
        }
        return value;
    }

    /**
     * Check if the property is multi-values (array of string)
     *
     * @param resource The resource to fetch property's value from
     * @param propertyName The property name
     * @return value This is boolean
     */
    public static boolean isPropertyMultiple(Resource resource, String propertyName) {
        boolean isPropertyMultiple = false;
        try {
            if (null != resource) {
                Node node = resource.adaptTo(Node.class);
                if (null != node) {
                    isPropertyMultiple = (node.hasProperty(propertyName) && node.getProperty(propertyName).isMultiple());
                }
            }
        } catch (RepositoryException e) {
            log.error(ERROR, e);
        }
        return isPropertyMultiple;
    }
}
