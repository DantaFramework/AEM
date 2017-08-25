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

import java.io.IOException;
import java.io.Reader;

/**
 * Danta Template Source Reader
 *
 * @author      jbarrera
 * @version     1.0.0
 * @since       2016-07-29
 */
public class DantaTemplateSourceReader {

    private static final int TEMPLATE_READER_BUFFER_SIZE = 2000;

    public DantaTemplateSourceReader() {
    }

    public String contentReader(Reader reader) throws IOException {
        StringBuilder contentAsString = new StringBuilder(TEMPLATE_READER_BUFFER_SIZE);
        try {
            char[] buffer = new char[TEMPLATE_READER_BUFFER_SIZE];
            int nrOfChars;
            if (reader != null) {
                while ((nrOfChars = reader.read(buffer, 0, TEMPLATE_READER_BUFFER_SIZE)) != -1) {
                    contentAsString.append(buffer, 0, nrOfChars);
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return contentAsString.toString();
    }
}
