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
import danta.api.ContentModel;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;

import java.util.Collections;
import java.util.Set;

import static danta.Constants.HIGHER_PRIORITY;
import static danta.Constants.LIST_CATEGORY;

/**
 * The abstraction context processor for handling item list
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2014-08-16
 */
@Component(componentAbstract = true)
@Service
public abstract class AbstractItemListContextProcessor<C extends ContentModel>
        extends AbstractCheckComponentCategoryContextProcessor<C>
        implements ListContextProcessor<C> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(LIST_CATEGORY));


    protected static final int PRIORITY = HIGHER_PRIORITY - 19;

    @Override
    public Set<String> anyOf() {
        return ANY_OF;
    }

    @Override
    public int priority() {
        return PRIORITY;
    }

}
