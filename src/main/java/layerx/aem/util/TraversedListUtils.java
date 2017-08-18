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

import com.day.cq.commons.Filter;
import com.day.cq.wcm.api.Page;

import java.util.*;

import static layerx.Constants.IS_CURRENT_PAGE;
import static layerx.aem.Constants.PATH_DETAILS_LIST_PATHS_PROPERTY_NAME;
import static layerx.aem.Constants.PATH_DETAILS_LIST_PATH_PROPERTY_NAME;

/**
 * Container for the CQ general objects.
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-04-16
 */
public final class TraversedListUtils {

    /**
     * Forbid instantiation
     */
    private TraversedListUtils() {}

    /**
     * Takes page, filter, depth, the current page path, and extracts a list of children paths
     *
     * @param page The page to extract the list of children paths from
     * @param filter The filter
     * @param depth The depth to recurse on
     * @param currentPage The path of the current page to flag is_current_page
     * @return extractedPaths This is a Collection of Map of Page Object
     */
    public static Collection<Map<String, Object>> extractPathList(Page page, Filter<Page> filter, int depth, String currentPage) {
        Collection<Map<String, Object>> pathList = new ArrayList<>();
        Iterator<Page> children = page.listChildren(filter);
        if (depth > 0) {
            while (children.hasNext()) {
                Page child = children.next();
                Map<String, Object> currentPath = new HashMap<>();
                Collection<Map<String, Object>> childPaths = extractPathList(child, filter, depth - 1, currentPage);
                String path = child.getPath();
                currentPath.put(PATH_DETAILS_LIST_PATH_PROPERTY_NAME, path);
                currentPath.put(PATH_DETAILS_LIST_PATHS_PROPERTY_NAME, childPaths);
                if (path.equals(currentPage)) {
                    currentPath.put(IS_CURRENT_PAGE,true);
                }
                pathList.add(currentPath);
            }
        }
        return pathList;
    }

    /**
     * Takes page, filter, depth, the current page path, excluded path, and extracts a list of children paths except the excluded path
     *
     * @param page The page to extract the list of children paths from
     * @param filter The filter
     * @param depth The depth to recurse on
     * @param currentPage The path of the current page to flag is_current_page
     * @param removeCurrentPage The path to be excluded
     * @return extractedPaths This is a Collection of Map of Page Object
     */
    public static Collection<Map<String, Object>> extractPathListCP(Page page, Filter<Page> filter, int depth, String currentPage, boolean removeCurrentPage) {
        Collection<Map<String, Object>> pathList = new ArrayList<>();
        Iterator<Page> children = page.listChildren(filter);
        if (depth > 0) {
            while (children.hasNext()) {
                Page child = children.next();
                Map<String, Object> currentPath = new HashMap<>();
                Collection<Map<String, Object>> childPaths = extractPathList(child, filter, depth - 1, currentPage);
                String path = child.getPath();

                if (!path.equals(currentPage)) {
                    currentPath.put(PATH_DETAILS_LIST_PATH_PROPERTY_NAME, path);
                    currentPath.put(PATH_DETAILS_LIST_PATHS_PROPERTY_NAME, childPaths);
                    pathList.add(currentPath);
                } else if (!removeCurrentPage) {
                    currentPath.put(PATH_DETAILS_LIST_PATH_PROPERTY_NAME, path);
                    currentPath.put(PATH_DETAILS_LIST_PATHS_PROPERTY_NAME, childPaths);
                    currentPath.put(IS_CURRENT_PAGE, true);
                    pathList.add(currentPath);
                }
            }
        }
        return pathList;
    }

    /**
     * Takes page, depth, the path of the current page, and extracts a list of children paths
     *
     * @param page The page to extract the children paths from
     * @param depth The depth to recurse on
     * @param currentPage The current path to checked
     * @return extractedPaths This is a Collection of Map of Page Object
     */
    public static Collection<Map<String, Object>> extractPathList(Page page, int depth, String currentPage) {
        return extractPathList(page, null, depth, currentPage);
    }

    /**
     * Takes page, depth, the path of the current page, and extracts a list of children paths
     *
     * @param page The page to extract the children paths from
     * @param depth The depth to recurse on
     * @param currentPage The current path to checked
     * @param removeCurrentPage The paths to be excluded
     * @return extractedPaths This is a Collection of Map of Page Object
     */
    public static Collection<Map<String, Object>> extractPathList(Page page, int depth, String currentPage, boolean removeCurrentPage) {
        return extractPathListCP(page, null, depth, currentPage, removeCurrentPage);
    }

}
