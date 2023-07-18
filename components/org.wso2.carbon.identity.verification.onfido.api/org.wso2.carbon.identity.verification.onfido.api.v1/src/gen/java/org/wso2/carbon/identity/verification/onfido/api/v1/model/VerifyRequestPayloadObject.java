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

package org.wso2.carbon.identity.verification.onfido.api.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class VerifyRequestPayloadObject  {
  
    private String id;
    private String status;
    private OffsetDateTime completedAtIso8601;
    private String href;

    /**
    **/
    public VerifyRequestPayloadObject id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "1d74df5a-00c2-45b0-8519-cd0076aca3c1", value = "")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
    **/
    public VerifyRequestPayloadObject status(String status) {

        this.status = status;
        return this;
    }
    
    @ApiModelProperty(example = "complete", value = "")
    @JsonProperty("status")
    @Valid
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    /**
    **/
    public VerifyRequestPayloadObject completedAtIso8601(OffsetDateTime completedAtIso8601) {

        this.completedAtIso8601 = completedAtIso8601;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("completed_at_iso8601")
    @Valid
    public OffsetDateTime getCompletedAtIso8601() {
        return completedAtIso8601;
    }
    public void setCompletedAtIso8601(OffsetDateTime completedAtIso8601) {
        this.completedAtIso8601 = completedAtIso8601;
    }

    /**
    **/
    public VerifyRequestPayloadObject href(String href) {

        this.href = href;
        return this;
    }
    
    @ApiModelProperty(value = "")
    @JsonProperty("href")
    @Valid
    public String getHref() {
        return href;
    }
    public void setHref(String href) {
        this.href = href;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VerifyRequestPayloadObject verifyRequestPayloadObject = (VerifyRequestPayloadObject) o;
        return Objects.equals(this.id, verifyRequestPayloadObject.id) &&
            Objects.equals(this.status, verifyRequestPayloadObject.status) &&
            Objects.equals(this.completedAtIso8601, verifyRequestPayloadObject.completedAtIso8601) &&
            Objects.equals(this.href, verifyRequestPayloadObject.href);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, completedAtIso8601, href);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class VerifyRequestPayloadObject {\n");
        
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    status: ").append(toIndentedString(status)).append("\n");
        sb.append("    completedAtIso8601: ").append(toIndentedString(completedAtIso8601)).append("\n");
        sb.append("    href: ").append(toIndentedString(href)).append("\n");
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

