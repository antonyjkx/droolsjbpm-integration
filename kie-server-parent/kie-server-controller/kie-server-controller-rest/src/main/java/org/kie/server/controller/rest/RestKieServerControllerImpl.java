/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.controller.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.*;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.storage.KieServerControllerStorage;
import org.kie.server.controller.impl.KieServerControllerImpl;
import org.kie.server.controller.impl.storage.InMemoryKieServerControllerStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.controller.rest.ControllerUtils.*;
import static org.kie.server.controller.rest.docs.ParameterSamples.*;

@Api(value = "Controller :: Server Controller")
@Path("/controller")
public class RestKieServerControllerImpl extends KieServerControllerImpl {

    private static final Logger logger = LoggerFactory.getLogger(RestKieServerControllerImpl.class);

    private KieServerControllerStorage storage = InMemoryKieServerControllerStorage.getInstance();

    @ApiOperation(value = "Connect a new Kie Server instance to Controller", response = KieServerSetup.class)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Unexpected error")})
    @PUT
    @Path("server/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response connectKieServer(@Context HttpHeaders headers,
                                     @ApiParam(name = "Kie Server instance identifier", required = true) @PathParam("id") String id,
                                     @ApiParam(name = "Kie Server instance information", required = true, examples =
                                     @Example(value = {
                                             @ExampleProperty(mediaType = JSON, value = KIE_SERVER_INFO_JSON),
                                             @ExampleProperty(mediaType = XML, value = KIE_SERVER_INFO_XML)
                                     })) String serverInfoPayload) {
        String contentType = getContentType(headers);
        logger.debug("Received connect request from server with id {}", id);
        KieServerInfo serverInfo = unmarshal(serverInfoPayload, contentType, KieServerInfo.class);
        logger.debug("Server info {}", serverInfo);
        KieServerSetup serverSetup = connect(serverInfo);

        logger.info("Server with id '{}' connected", id);
        String response = marshal(contentType, serverSetup);

        logger.debug("Returning response for connect of server '{}': {}", id, response);
        return createCorrectVariant(response, headers, Response.Status.CREATED);
    }

    @ApiOperation(value = "Remove a Kie Server instance from Controller")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Unexpected error")})
    @DELETE
    @Path("server/{id}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response disconnectKieServer(@Context HttpHeaders headers,
                                        @ApiParam(name = "Kie Server instance identifier", required = true) @PathParam("id") String id,
                                        @ApiParam(name = "Kie Server instance URL", required = true) @QueryParam("location") String serverLocation) {
        try {
            KieServerInfo serverInfo = new KieServerInfo(id, "", "", Collections.<String>emptyList(), URLDecoder.decode(serverLocation, "UTF-8"));
            disconnect(serverInfo);
            logger.info("Server with id '{}' disconnected", id);
        } catch (UnsupportedEncodingException e) {
            logger.debug("Cannot URL decode kie server location due to unsupported encoding exception", e);
        }
        return null;
    }

}
