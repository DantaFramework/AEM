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

package danta.aem.contextprocessors.lists;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.common.collect.Sets;
import danta.aem.util.TraversedListUtils;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.configuration.Configuration;
import danta.api.exceptions.ProcessException;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * This Context Processor adds to the content model a list of page paths in 'list.pageRefs'
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2014-08-16
 */
@Component
@Service
public class AddTraversedPageReferencesContextProcessor
        extends AbstractItemListContextProcessor<TemplateContentModel> {

    private static final Set<String> ALL_OF = Collections.unmodifiableSet(Sets.newHashSet(TRAVERSED_LIST_CATEGORY));

    protected static final int PRIORITY = AddItemListContextProcessor.PRIORITY - 20;

    @Override
    public Set<String> allOf() {
        return ALL_OF;
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();
            if (resource != null) {
                ResourceResolver resourceResolver = resource.getResourceResolver();
                PageManager pageManager = resourceResolver.adaptTo(PageManager.class);
                String pathRefListContentKeyName = getPathRefListKeyName(resource);
                if (contentModel.has(pathRefListContentKeyName)) {
                    String pathRef = contentModel.getAsString(pathRefListContentKeyName);

                    Collection<Map<String, Object>> pathList = new ArrayList<>();
                    if (pathRef != null) {
                        Page page = pageManager.getContainingPage(pathRef);
                        int depth = LIST_DEFAULT_DEPTH;
                        String depthListContentKeyName = getDepthKeyName(resource);
                        if (contentModel.has(depthListContentKeyName)) {
                            depth =  Integer.parseInt(contentModel.getAsString(depthListContentKeyName));
                        }
                        if (null != page) {
                            String currentPage = contentModel.getAsString(PAGE + DOT + PATH);
                            boolean removeCurrentPage = false;
                            if (contentModel.has(REMOVE_CURRENT_PAGE_PATH_CONFIG_KEY) &&
                                    contentModel.getAsString(REMOVE_CURRENT_PAGE_PATH_CONFIG_KEY).equals(TRUE)){
                                removeCurrentPage = true;
                            }
                            pathList = extractPathList(page, depth, currentPage, removeCurrentPage);
                        }
                    }
                    contentModel.set(LIST_PROPERTIES_KEY + DOT + PAGEREFS_CONTENT_KEY_NAME, pathList);
                }
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    /**
     * Looks for the key name under config. If not found, gets the default one
     * @param resource the request resource
     * @return the name of the key where the traversed list base path is stored
     * @throws Exception
     */
    protected String getPathRefListKeyName(Resource resource) throws Exception {
        Configuration configuration = configurationProvider.getFor(resource.getResourceType());
        String configurationPathRefListKeyName = configuration.asString(PATHREF_CONFIGURATION_PROPERTY_NAME);
        return (StringUtils.isNotEmpty(configurationPathRefListKeyName) ? configurationPathRefListKeyName : PATHREF_LIST_CONTENT_KEY);
    }

    /**
     * Looks for the depth under config. If not found, gets the default one
     * @param resource the request resource
     * @return the depth of the traversed tree
     * @throws Exception
     */
    protected String getDepthKeyName(Resource resource) throws Exception {
        Configuration configuration = configurationProvider.getFor(resource.getResourceType());
        String configurationDepthKeyName = configuration.asString(DEPTH_CONFIGURATION_PROPERTY_NAME);
        return (StringUtils.isNotEmpty(configurationDepthKeyName)) ? configurationDepthKeyName : DEPTH_LIST_CONTENT_KEY;
    }

    /**
     *
     * @param page
     * @param depth
     * @return
     */
    protected Collection<Map<String, Object>> extractPathList(Page page, int depth, String currentPage, boolean removeCurrentPage) {
        return TraversedListUtils.extractPathList(page, depth, currentPage, removeCurrentPage);
    }

}
