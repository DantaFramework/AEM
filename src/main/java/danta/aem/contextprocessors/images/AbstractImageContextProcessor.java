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

package danta.aem.contextprocessors.images;

import com.google.common.collect.Sets;
import danta.api.ContentModel;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;

import java.util.Collections;
import java.util.Set;

import static danta.Constants.IMAGE_CATEGORY;

/**
 * The abstraction for image context processor
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2014-09-04
 */
public abstract class AbstractImageContextProcessor<C extends ContentModel>
        extends AbstractCheckComponentCategoryContextProcessor<C>
        implements ImageContextProcessor<C> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(IMAGE_CATEGORY));

    @Override
    public Set<String> anyOf() {
        return ANY_OF;
    }
}
