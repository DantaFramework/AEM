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

import com.day.cq.tagging.Tag;
import com.day.cq.tagging.TagManager;
import com.day.cq.wcm.api.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.Arrays;

import static danta.Constants.BLANK;
import static danta.aem.Constants.SLING_VANITY_PATH;

/**
 * Page Utility class, contained generic methods for handling CQ Page.
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2014-03-27
 */
public class PageUtils {

    /**
     * Forbid instantiation
     */
    private PageUtils() {}

    /**
     *
     * @param page This is a Page
     * @return navigationTitle This is the page title
     */
    public static String getNavigationTitle(Page page) {
        String navigationTitle = null;
        if (null != page) {
            navigationTitle = page.getNavigationTitle();
            if (null == navigationTitle) {
                navigationTitle = page.getPageTitle() == null ? page.getTitle() : page.getPageTitle();
            }
        }
        return navigationTitle;
    }

    /**
     * Allows the developer to directly get a page property from a page object (Supports null parameters).
     *
     * @param page         AEM page object
     * @param propertyName Property name
     * @return The property value or a blank string if the property is not present.
     */
    public static String getPageProperty(Page page, String propertyName) {
        String pageProperty = BLANK;
        if (page != null) {
            ValueMap pageProperties = page.getProperties();
            if ((pageProperties != null) && (propertyName != null)) {
                pageProperty = pageProperties.get(propertyName, BLANK);
            }
        }
        return pageProperty;

    }

    /**
     * Get keywords from the cq:tags Object
     *
     * @param objProperty This is the cq:tags Object
     * @param tm This is a Tag Manager instance
     * @return keywords This is the meta keywords set on the page
     */
    public static String getKeywords(Object objProperty, TagManager tm) {
        String keywords = "";
        if (objProperty != null) {
            ArrayList<?> tags = (ArrayList<?>) objProperty;
            if (tags != null) {
                for (Object obj : tags) {
                    String value = obj.toString();
                    if (keywords != null && keywords.length() > 0) {
                        keywords += ", ";
                    }
                    boolean noTagInfo = false;
                    Tag tag = tm.resolve(value);
                    if (tag != null) {
                        if (tag.getTitle() != null && tag.getTitle().length() > 0) {
                            value = tag.getTitle();
                        } else if (tag.getName() != null && tag.getName().length() > 0) {
                            value = tag.getName();
                        } else {
                            noTagInfo = true;
                        }
                    } else {
                        noTagInfo = true;
                    }
                    if (noTagInfo) {
                        if (value != null && value.length() > 0) {
                            if (value.indexOf("/") != -1) {
                                value = StringUtils.substringAfter(value, "/");
                            } else if (value.indexOf(":") != -1) {
                                value = StringUtils.substringAfter(value, ":");
                            }
                        }
                    }
                    keywords += value;
                }
            }
        }
        return keywords;
    }

    /**
     * Get CQ Tags set on the page.
     *
     * @param pageContent This is a map of page content
     * @return keywords This is the comma separated list of CQ tags
     */
    public static String getTags(Object tagsObj) {
        String keywords = "";
        if (tagsObj != null) {
            ArrayList<?> tags = (ArrayList<?>) tagsObj;
            if (tags != null) {
                for (Object obj : tags) {
                    String value = obj.toString();
                    if (keywords != null && keywords.length() > 0) {
                        keywords += ", ";
                    }
                    keywords += value;
                }
            }
        }
        return keywords;
    }

    /**
     * Returns the vanity URLs stored in the page node.
     *
     * @param page The page object
     * @return vanityPaths The vanityURLs either as a string or list.
     */
    public static Object getVanityURLs(Page page) {
        Object vanityPath = page.getProperties().get(SLING_VANITY_PATH);
        if(vanityPath != null) {
            if(vanityPath.getClass().isArray()) {

                return Arrays.asList((String[]) vanityPath);
            } else {

                return vanityPath;
            }
        }

        return null;
    }
}
