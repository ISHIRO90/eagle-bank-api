package com.eaglebank.api.generated.model;

import java.net.URI;
import java.util.Objects;
import com.eaglebank.api.generated.model.BadRequestErrorResponseDetailsInner;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * BadRequestErrorResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-07-06T13:05:12.066616+01:00[Europe/London]")
public class BadRequestErrorResponse {

  private String message;

  @Valid
  private List<@Valid BadRequestErrorResponseDetailsInner> details = new ArrayList<>();

  public BadRequestErrorResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BadRequestErrorResponse(String message, List<@Valid BadRequestErrorResponseDetailsInner> details) {
    this.message = message;
    this.details = details;
  }

  public BadRequestErrorResponse message(String message) {
    this.message = message;
    return this;
  }

  /**
   * Get message
   * @return message
  */
  @NotNull 
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public BadRequestErrorResponse details(List<@Valid BadRequestErrorResponseDetailsInner> details) {
    this.details = details;
    return this;
  }

  public BadRequestErrorResponse addDetailsItem(BadRequestErrorResponseDetailsInner detailsItem) {
    if (this.details == null) {
      this.details = new ArrayList<>();
    }
    this.details.add(detailsItem);
    return this;
  }

  /**
   * Get details
   * @return details
  */
  @NotNull @Valid 
  @JsonProperty("details")
  public List<@Valid BadRequestErrorResponseDetailsInner> getDetails() {
    return details;
  }

  public void setDetails(List<@Valid BadRequestErrorResponseDetailsInner> details) {
    this.details = details;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BadRequestErrorResponse badRequestErrorResponse = (BadRequestErrorResponse) o;
    return Objects.equals(this.message, badRequestErrorResponse.message) &&
        Objects.equals(this.details, badRequestErrorResponse.details);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, details);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BadRequestErrorResponse {\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

