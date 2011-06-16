/*
 * Copyright 2009-2010 MBTE Sweden AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbte.gretty.httpserver

import org.jboss.netty.channel.local.LocalAddress
import org.jboss.netty.handler.codec.http.HttpMethod
import org.mbte.gretty.httpclient.HttpRequestHelper

class TemplateTest extends GrettyServerTestCase {

    protected void buildServer() {
        File root = ["."]
        root = root.canonicalFile
        def tempFile = File.createTempFile("temp_", "_script.gpptl", root)
        tempFile.text = """\
Request path: \${request.uri}\
URI: \$uri\
<%
    response.addHeader "Template", "true"
%>"""
        tempFile.deleteOnExit()

        def tempFile2 = File.createTempFile("temp_", "_script.groovy", root)
        tempFile2.text = """\
        out.print request.path.toUpperCase()
"""
        tempFile.deleteOnExit()

        server = new GrettyServer ()
        server.groovy = [
            default: {
                response.text = template(request.path.endsWith("lala") ? tempFile.absolutePath : tempFile2.absolutePath) { binding ->
                    binding.uri = request.uri.toUpperCase ()
                }
            }
        ]
    }

    void testTemplate() {
        GrettyHttpRequest req = [method:HttpMethod.GET, uri:"/template/lala"]
        doTest(req) { GrettyHttpResponse response ->
            assert response.contentText == "Request path: /template/lalaURI: /TEMPLATE/LALA"
            assert response.getHeader("Template") == "true"
        }
    }

    void testScript() {
        GrettyHttpRequest req = [method:HttpMethod.GET, uri:"/template/mama"]
        doTest(req) { GrettyHttpResponse response ->
            assert response.contentText == "/TEMPLATE/MAMA"
        }
    }
}
