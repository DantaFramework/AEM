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

import com.google.common.collect.Sets;
import danta.aem.util.PropertyUtils;
import danta.api.ExecutionContext;
import danta.api.TemplateContentModel;
import danta.api.configuration.Configuration;
import danta.api.configuration.Mode;
import danta.api.exceptions.ProcessException;
import danta.core.contextprocessors.AbstractCheckComponentCategoryContextProcessor;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.*;

import static danta.Constants.DOT;
import static danta.Constants.HIGHEST_PRIORITY;
import static danta.aem.Constants.*;

/**
 * This CP allows to add the data from sling models in the content model,
 * when it has the 'xk_slingModels' property in the xk.config node with the
 * list of models.
 * It add the data on the 'model' context, in a property with the name of the model class.
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2014-09-04
 */
@Component
@Service
public class AddSlingModelsPropertiesContextProcessor
        extends AbstractCheckComponentCategoryContextProcessor<TemplateContentModel> {

    private static final Set<String> ANY_OF = Collections.unmodifiableSet(Sets.newHashSet(SLING_MODELS_CATEGORY));

    @Reference
    private ModelFactory modelFactory;

    @Override
    public Set<String> anyOf() {
        return ANY_OF;
    }

    @Override
    public int priority() {
        return HIGHEST_PRIORITY;
    }

    @Override
    public void process(final ExecutionContext executionContext, final TemplateContentModel contentModel)
            throws ProcessException {
        try {
            SlingHttpServletRequest request = (SlingHttpServletRequest) executionContext.get(SLING_HTTP_REQUEST);
            Resource resource = request.getResource();

            Configuration config = configurationProvider.getFor(request.getResource().getResourceType());
            Collection<String> models = config.asStrings(SLING_MODELS_CONFIG_PROPERTY_NAME, Mode.MERGE);

            Object model;
            String modelName = "";
            Map<String, Object> modelData = new HashMap<>();

            for (String modelId : models) {
                Class<?> modelClass= Class.forName(modelId);

                if (modelFactory.isModelClass(modelClass)) {
                    model = resource.adaptTo(modelClass);
                    modelData = getModelProperties(model);
                    modelName =  modelClass.getSimpleName();
                    contentModel.set(SLING_MODEL_PROPERTIES_KEY + DOT + modelName, modelData);
                } else {
                    log.error("{} is not a Sling Model", modelClass);
                }
            }

        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    private Map<String, Object> getModelProperties(Object model) {
        try {
            Map<String, Object> modelProperties = new HashMap<>();
            Arrays.stream(Introspector.getBeanInfo(model.getClass(), Object.class)
                    .getPropertyDescriptors())
                    .filter(pd -> Objects.nonNull(pd.getReadMethod()))
                    .forEach(pd -> {
                        try {
                            Object value = pd.getReadMethod().invoke(model);
                            if (value != null) {
                                if (value instanceof Collection) {
                                    Map<String, Object> resourcesProps = new HashMap<>();
                                    for (Object item : (ArrayList) value) {
                                        if (item instanceof Resource) {
                                            Resource itemResource = (Resource) item;
                                            Map<String, Object> resProperties = (itemResource != null) ? PropertyUtils.propsToMap(itemResource) : new HashMap<String, Object>();
                                            resourcesProps.put(itemResource.getName(), resProperties);
                                        }
                                    }
                                    modelProperties.put(pd.getName(), resourcesProps);
                                } else if (value instanceof Resource) {
                                    Resource resource = (Resource) value;
                                    Map<String, Object> resProperties = (resource != null) ? PropertyUtils.propsToMap(resource) : new HashMap<String, Object>();
                                    modelProperties.put(pd.getName(), resProperties);

                                } else {
                                    modelProperties.put(pd.getName(), value);
                                }
                            }
                        } catch (Exception e) {}
                    });
            return modelProperties;
        } catch (IntrospectionException e) {
            return Collections.emptyMap();
        }
    }
}

