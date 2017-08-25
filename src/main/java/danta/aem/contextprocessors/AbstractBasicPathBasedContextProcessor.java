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

package danta.aem.contextprocessors;

import danta.api.ContentModel;
import danta.api.ExecutionContext;
import danta.api.exceptions.AcceptsException;
import org.apache.sling.api.SlingHttpServletRequest;

import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * Abstraction for basic path based context processor
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-08
 */
public abstract class AbstractBasicPathBasedContextProcessor<C extends ContentModel>
        extends AbstractCheckResourceExistenceContextProcessor<C> {

    @Override
    public boolean accepts(final ExecutionContext executionContext)
            throws AcceptsException {
        SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
        return super.accepts(executionContext) ? request.getResource().getPath().startsWith(requiredRoot()) : false;
    }

    /**
     * This method must be used to set the base root Path to be compared on the accepts() method.
     *
     * @return the Path String.
     */
    public abstract String requiredRoot();
}
