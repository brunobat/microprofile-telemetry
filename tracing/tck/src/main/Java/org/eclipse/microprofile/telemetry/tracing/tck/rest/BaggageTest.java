/*
 * Copyright (c) 2016-2022 Contributors to the Eclipse Foundation
 *
 *  See the NOTICE file(s) distributed with this work for additional
 *  information regarding copyright ownership.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  You may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package tck.rest;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URL;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import tck.InMemorySpanExporter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.opentelemetry.api.baggage.Baggage;

@ExtendWith(ArquillianExtension.class)
class BaggageTest {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class);
    }

    @ArquillianResource
    URL url;
    @Inject
    InMemorySpanExporter spanExporter;

    @BeforeEach
    void setUp() {
        spanExporter.reset();
    }

    @Test
    void baggage() {
        WebTarget target = ClientBuilder.newClient().target(url.toString() + "baggage");
        Response response = target.request().header("baggage", "user=naruto").get();
        Assertions.assertEquals(HTTP_OK, response.getStatus());

        spanExporter.getFinishedSpanItems(2);
    }

    @Path("/baggage")
    public static class BaggageResource {
        @Inject
        Baggage baggage;

        @GET
        public Response baggage() {
            Assertions.assertEquals("naruto", baggage.getEntryValue("user"));
            return Response.ok().build();
        }
    }

    @ApplicationPath("/")
    public static class RestApplication extends Application {

    }
}