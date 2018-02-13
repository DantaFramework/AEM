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

package danta.aem.contextprocessors.domprocesors;

import danta.api.DOMProcessor;

import static danta.Constants.LOW_PRIORITY;

/**
 * This abstract class provides a default priority implementation. Every DOMProcessor is expected to extend from this
 * class (or a subclass).
 *
 * @author      joshuaoransky
 * @version     1.0.0
 * @since       2014-09-04
 */
public abstract class AbstractDOMProcessor
        implements DOMProcessor {
    /**
     * This method returns the priority of this DOMProcessor. The priority defines the order in which the
     * DOMProcessors will be executed.
     * @return
     */
    @Override
    public int priority() {
        return LOW_PRIORITY;
    }
}
