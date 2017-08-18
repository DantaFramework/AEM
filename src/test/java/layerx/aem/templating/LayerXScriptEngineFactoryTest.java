/**
 * LayerX AEM Bundle
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

package layerx.aem.templating;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Author:  jbarrera
 * Date:    8/25/16
 * Purpose:
 * Location:
 */
public class LayerXScriptEngineFactoryTest {

    private LayerXScriptEngineFactory scriptEngineFactory;


    @Before
    public void setUp() throws Exception {
        scriptEngineFactory = new LayerXScriptEngineFactory();
    }

    @Test
    public void testLayerXScriptEngineProperties() throws Exception {
        Assert.assertEquals("layerx", scriptEngineFactory.getLanguageName());
        Assert.assertEquals("1.0", scriptEngineFactory.getLanguageVersion());
        Assert.assertArrayEquals(new String[]{"lx"},
                scriptEngineFactory.getExtensions().toArray());
        Assert.assertEquals("LayerX Script Engine", scriptEngineFactory.getEngineName());
    }
}
