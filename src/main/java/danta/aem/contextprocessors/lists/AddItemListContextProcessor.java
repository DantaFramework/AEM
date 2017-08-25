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

import com.google.common.collect.Lists;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.exceptions.ProcessException;
import net.minidev.json.JSONValue;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import java.util.ArrayList;
import java.util.List;

import static danta.Constants.*;

/**
 * This Context Processor adds to the content model a list of items in 'list.items'.
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2014-08-16
 */
@Component
@Service
public class AddItemListContextProcessor
        extends AbstractItemListContextProcessor<TemplateContentModel> {

    public static final int PRIORITY = AbstractItemListContextProcessor.PRIORITY - 20;

    @Override
    public int priority() {
        return PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, TemplateContentModel contentModel)
            throws ProcessException {
        try {
            String itemsContentKey = CONTENT + DOT + ITEMS_KEY_NAME;
            if (contentModel.has(CONFIG_PROPERTIES_KEY + DOT + ITEMS_CONFIG_KEY)) {
                itemsContentKey = contentModel.getAsString(CONFIG_PROPERTIES_KEY + DOT + ITEMS_CONFIG_KEY);
            }

            Object o = contentModel.get(itemsContentKey);
            if (o instanceof ArrayList) {
                List list = Lists.newArrayList();
                for (Object item : (ArrayList) o) {
                    if (item instanceof String) {
                        String value = (String) item;
                        list.add(JSONValue.isValidJsonStrict(value)? JSONValue.parse(value) : value);
                    }
                }
                contentModel.set(ITEM_LIST_KEY_NAME, list);
            } else if (o instanceof String) {
                String value = (String) o;
                contentModel.set(ITEM_LIST_KEY_NAME,
                        Lists.newArrayList(JSONValue.isValidJsonStrict(value)? JSONValue.parse(value) : value));
            } else if (o instanceof String[]) {
                List list = Lists.newArrayList();
                for (Object item : (String[]) o) {
                    if (item instanceof String) {
                        String value = (String) item;
                        list.add(JSONValue.isValidJsonStrict(value)? JSONValue.parse(value) : value);
                    }
                }
                contentModel.set(ITEM_LIST_KEY_NAME, list);
            }
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

}
