package com.eaglebank.api.generated.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateUserRequestAddress
 */

@JsonTypeName("CreateUserRequest_address")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-07-06T13:05:12.066616+01:00[Europe/London]")
public class CreateUserRequestAddress {

  private String line1;

  private String line2;

  private String line3;

  private String town;

  private String county;

  private String postcode;

  public CreateUserRequestAddress() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public CreateUserRequestAddress(String line1, String town, String county, String postcode) {
    this.line1 = line1;
    this.town = town;
    this.county = county;
    this.postcode = postcode;
  }

  public CreateUserRequestAddress line1(String line1) {
    this.line1 = line1;
    return this;
  }

  /**
   * Get line1
   * @return line1
  */
  @NotNull 
  @JsonProperty("line1")
  public String getLine1() {
    return line1;
  }

  public void setLine1(String line1) {
    this.line1 = line1;
  }

  public CreateUserRequestAddress line2(String line2) {
    this.line2 = line2;
    return this;
  }

  /**
   * Get line2
   * @return line2
  */
  
  @JsonProperty("line2")
  public String getLine2() {
    return line2;
  }

  public void setLine2(String line2) {
    this.line2 = line2;
  }

  public CreateUserRequestAddress line3(String line3) {
    this.line3 = line3;
    return this;
  }

  /**
   * Get line3
   * @return line3
  */
  
  @JsonProperty("line3")
  public String getLine3() {
    return line3;
  }

  public void setLine3(String line3) {
    this.line3 = line3;
  }

  public CreateUserRequestAddress town(String town) {
    this.town = town;
    return this;
  }

  /**
   * Get town
   * @return town
  */
  @NotNull 
  @JsonProperty("town")
  public String getTown() {
    return town;
  }

  public void setTown(String town) {
    this.town = town;
  }

  public CreateUserRequestAddress county(String county) {
    this.county = county;
    return this;
  }

  /**
   * Get county
   * @return county
  */
  @NotNull 
  @JsonProperty("county")
  public String getCounty() {
    return county;
  }

  public void setCounty(String county) {
    this.county = county;
  }

  public CreateUserRequestAddress postcode(String postcode) {
    this.postcode = postcode;
    return this;
  }

  /**
   * Get postcode
   * @return postcode
  */
  @NotNull 
  @JsonProperty("postcode")
  public String getPostcode() {
    return postcode;
  }

  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateUserRequestAddress createUserRequestAddress = (CreateUserRequestAddress) o;
    return Objects.equals(this.line1, createUserRequestAddress.line1) &&
        Objects.equals(this.line2, createUserRequestAddress.line2) &&
        Objects.equals(this.line3, createUserRequestAddress.line3) &&
        Objects.equals(this.town, createUserRequestAddress.town) &&
        Objects.equals(this.county, createUserRequestAddress.county) &&
        Objects.equals(this.postcode, createUserRequestAddress.postcode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(line1, line2, line3, town, county, postcode);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateUserRequestAddress {\n");
    sb.append("    line1: ").append(toIndentedString(line1)).append("\n");
    sb.append("    line2: ").append(toIndentedString(line2)).append("\n");
    sb.append("    line3: ").append(toIndentedString(line3)).append("\n");
    sb.append("    town: ").append(toIndentedString(town)).append("\n");
    sb.append("    county: ").append(toIndentedString(county)).append("\n");
    sb.append("    postcode: ").append(toIndentedString(postcode)).append("\n");
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

