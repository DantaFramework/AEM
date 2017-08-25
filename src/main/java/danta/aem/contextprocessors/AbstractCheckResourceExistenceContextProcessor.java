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
import danta.core.contextprocessors.AbstractContextProcessor;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceUtil;

import static danta.Constants.HIGH_PRIORITY;
import static danta.aem.Constants.SLING_HTTP_REQUEST;

/**
 * This class is intended to provide an easy to use accept method that returns
 * true only if the current resource exists, that is the current resource is
 * different than null and is not a synthetic resource.
 * <p/>
 * Context Processors that only require an existing, non-synthetic resource
 * for execution should extend from this class, in order to take advantage of
 * the accepts method defined in this abstract class.
 * <p/>
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2013-11-08
 */
public abstract class AbstractCheckResourceExistenceContextProcessor<C extends ContentModel>
        extends AbstractContextProcessor<C> {

    @Override
    public boolean accepts(final ExecutionContext executionContext)
            throws AcceptsException {
        SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
        Resource resource = request.getResource();
        return (mustExist()) ? (resource != null && (!ResourceUtil.isSyntheticResource(resource))) : true;
    }

    protected boolean mustExist() {
        return true;
    }

    @Override
    public int priority() {
        return HIGH_PRIORITY;
    }
}
