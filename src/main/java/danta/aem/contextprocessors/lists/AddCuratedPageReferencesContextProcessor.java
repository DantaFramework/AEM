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

import com.google.common.collect.Sets;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.exceptions.ProcessException;
import net.minidev.json.JSONObject;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static danta.Constants.*;

/**
 * This Context Processor adds to the content model a list of page paths in 'list.pageRefs'.
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2014-08-16
 */
@Component
@Service
public class AddCuratedPageReferencesContextProcessor
        extends AbstractItemListContextProcessor<TemplateContentModel> {

    private static final Set<String> ALL_OF =
            Collections.unmodifiableSet(Sets.newHashSet(LIST_CATEGORY, CURATED_LIST_CATEGORY));

    public static final int PRIORITY = AddItemListContextProcessor.PRIORITY - 20;

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
            // Add page path references
            if (contentModel.has(ITEM_LIST_KEY_NAME)) {
                Object pathRefs = contentModel.get(ITEM_LIST_KEY_NAME);

                Collection<Map<String, Object>> pathList = new ArrayList<>();
                String currentPage = contentModel.getAsString(PAGE + DOT + PATH);

                if (pathRefs instanceof Collection) {
                    for(Object pathRef : (Collection<Object>) pathRefs) {
                        JSONObject pagePath = new JSONObject();
                        String path = "";

                        if (pathRef instanceof String) {
                            path = pathRef.toString();
                        } else if (pathRef instanceof JSONObject) {
                            JSONObject a = (JSONObject) pathRef;
                            Object value = a.get(PATH);
                            if ( value != null ) {
                                path = value.toString();
                            }
                        }

                        if (!path.equals(currentPage)) {
                            pagePath.put(PATH, path);
                            pathList.add(pagePath);
                        } else if (!(contentModel.has(REMOVE_CURRENT_PAGE_PATH_CONFIG_KEY)
                                && contentModel.getAsString(REMOVE_CURRENT_PAGE_PATH_CONFIG_KEY).equals(TRUE))){
                            pagePath.put(IS_CURRENT_PAGE, true);
                            pagePath.put(PATH, path);
                            pathList.add(pagePath);

                        }
                    }
                }
                contentModel.set(LIST_PROPERTIES_KEY + DOT + PAGEREFS_CONTENT_KEY_NAME, pathList);
            }

        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }
}
