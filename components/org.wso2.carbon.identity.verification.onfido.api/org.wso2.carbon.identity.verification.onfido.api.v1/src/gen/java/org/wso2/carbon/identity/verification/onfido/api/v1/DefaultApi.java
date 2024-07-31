/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.verification.onfido.api.v1;

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import java.io.InputStream;
import java.util.List;

import org.wso2.carbon.identity.verification.onfido.api.v1.model.Error;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequest;
import org.wso2.carbon.identity.verification.onfido.api.v1.DefaultApiService;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import io.swagger.annotations.*;

import javax.validation.constraints.*;

@Path("/")
@Api(description = "The  API")

public class DefaultApi  {

    @Autowired
    private DefaultApiService delegate;

    @Valid
    @POST
    @Path("/{idvp-id}/verify")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @ApiOperation(value = "Get the identity verification claims of a user", notes = "This API provides the capability to perform Identity Verification through onfido. ", response = Void.class, tags={ "Identity Verification" })
    @ApiResponses(value = { 
        @ApiResponse(code = 200, message = "Ok", response = Void.class),
        @ApiResponse(code = 400, message = "Invalid status value", response = Error.class),
        @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
        @ApiResponse(code = 403, message = "Forbidden", response = Void.class),
        @ApiResponse(code = 404, message = "Not Found", response = Error.class),
        @ApiResponse(code = 500, message = "Server Error", response = Error.class)
    })
    public Response verify(    @Valid @NotNull(message = "Property  cannot be null.")  @ApiParam(value = "SHA2 signature header for verification" ,required=true)@HeaderParam("X-SHA2-Signature") String xSHA2Signature, @ApiParam(value = "Id of the identity verification provider",required=true) @PathParam("idvp-id") String idvpId, @ApiParam(value = "Verify an identity" ) @Valid VerifyRequest verifyRequest) {

        return delegate.verify(xSHA2Signature,  idvpId,  verifyRequest );
    }

}
