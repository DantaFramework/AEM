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

package danta.aem;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Deactivate;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AEM Bundle Activator.
 *
 * @author      Danta Team
 * @version     1.0.0
 * @since       2017-08-08
 */
public class AEMActivator
        implements BundleActivator {

    // Private variables
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    public void start(BundleContext bundleContext)
            throws Exception {
        log.info("Activated!!!");
    }

    @Deactivate
    public void stop(BundleContext bundleContext)
            throws Exception {
        log.info("DeActivated!!!");
    }

}
