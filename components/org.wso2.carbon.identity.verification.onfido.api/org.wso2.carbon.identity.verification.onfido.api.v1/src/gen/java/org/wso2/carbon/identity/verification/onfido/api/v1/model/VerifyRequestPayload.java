/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.verification.onfido.api.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequestPayloadObject;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class VerifyRequestPayload  {
  
    private String resourceType;
    private String action;
    private VerifyRequestPayloadObject _object;
    private Map<String, Object> resource = new HashMap<>();


    /**
    * The resource type affected by the event executed from Onfido.
    **/
    public VerifyRequestPayload resourceType(String resourceType) {

        this.resourceType = resourceType;
        return this;
    }
    
    @ApiModelProperty(example = "workflow_run", required = true, value = "The resource type affected by the event executed from Onfido.")
    @JsonProperty("resource_type")
    @Valid
    @NotNull(message = "Property resourceType cannot be null.")

    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    /**
    * The event that triggered this webhook.
    **/
    public VerifyRequestPayload action(String action) {

        this.action = action;
        return this;
    }
    
    @ApiModelProperty(example = "workflow_run.completed", required = true, value = "The event that triggered this webhook.")
    @JsonProperty("action")
    @Valid
    @NotNull(message = "Property action cannot be null.")

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    /**
    **/
    public VerifyRequestPayload _object(VerifyRequestPayloadObject _object) {

        this._object = _object;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("object")
    @Valid
    @NotNull(message = "Property _object cannot be null.")

    public VerifyRequestPayloadObject getObject() {
        return _object;
    }
    public void setObject(VerifyRequestPayloadObject _object) {
        this._object = _object;
    }

    /**
    **/
    public VerifyRequestPayload resource(Map<String, Object> resource) {

        this.resource = resource;
        return this;
    }
    
    @ApiModelProperty(required = true, value = "")
    @JsonProperty("resource")
    @Valid
    @NotNull(message = "Property resource cannot be null.")

    public Map<String, Object> getResource() {
        return resource;
    }
    public void setResource(Map<String, Object> resource) {
        this.resource = resource;
    }


    public VerifyRequestPayload putResourceItem(String key, Object resourceItem) {
        this.resource.put(key, resourceItem);
        return this;
    }

    

    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VerifyRequestPayload verifyRequestPayload = (VerifyRequestPayload) o;
        return Objects.equals(this.resourceType, verifyRequestPayload.resourceType) &&
            Objects.equals(this.action, verifyRequestPayload.action) &&
            Objects.equals(this._object, verifyRequestPayload._object) &&
            Objects.equals(this.resource, verifyRequestPayload.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, action, _object, resource);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class VerifyRequestPayload {\n");
        
        sb.append("    resourceType: ").append(toIndentedString(resourceType)).append("\n");
        sb.append("    action: ").append(toIndentedString(action)).append("\n");
        sb.append("    _object: ").append(toIndentedString(_object)).append("\n");
        sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

