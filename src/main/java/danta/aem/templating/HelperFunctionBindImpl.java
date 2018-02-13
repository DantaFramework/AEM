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

package danta.aem.templating;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.ReferenceCardinality;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper function binder implementer
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2016-10-10
 */
@Component(service = HelperFunctionBind.class)
public class HelperFunctionBindImpl
        implements HelperFunctionBind {

    protected ComponentContext componentContext;

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, bind = "bindHelperFunction", unbind = "unbindHelperFunction", service = HelperFunction.class, policy = ReferencePolicy.DYNAMIC)
    private List<HelperFunction> helpers = new ArrayList<>();

    public List<HelperFunction> getHelpers()
            throws Exception {
        return helpers;
    }

    @Activate
    protected void activate(ComponentContext componentContext)
            throws Exception {
        this.componentContext = componentContext;
    }

    private void bindHelperFunction(HelperFunction helper) {
        helpers.add(helper);
    }

    private void unbindHelperFunction(HelperFunction helper) {
        helpers.remove(helper);
    }

}
