package com.eaglebank.api.generated.model;

import java.net.URI;
import java.util.Objects;
import com.eaglebank.api.generated.model.CreateUserRequestAddress;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * UserResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-07-06T13:05:12.066616+01:00[Europe/London]")
public class UserResponse {

  private String id;

  private String name;

  private CreateUserRequestAddress address;

  private String phoneNumber;

  private String email;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdTimestamp;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedTimestamp;

  public UserResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public UserResponse(String id, String name, CreateUserRequestAddress address, String phoneNumber, String email, OffsetDateTime createdTimestamp, OffsetDateTime updatedTimestamp) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.phoneNumber = phoneNumber;
    this.email = email;
    this.createdTimestamp = createdTimestamp;
    this.updatedTimestamp = updatedTimestamp;
  }

  public UserResponse id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  @NotNull 
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public UserResponse name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  */
  @NotNull 
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UserResponse address(CreateUserRequestAddress address) {
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
  */
  @NotNull @Valid 
  @JsonProperty("address")
  public CreateUserRequestAddress getAddress() {
    return address;
  }

  public void setAddress(CreateUserRequestAddress address) {
    this.address = address;
  }

  public UserResponse phoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
    return this;
  }

  /**
   * Get phoneNumber
   * @return phoneNumber
  */
  @NotNull 
  @JsonProperty("phoneNumber")
  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public UserResponse email(String email) {
    this.email = email;
    return this;
  }

  /**
   * Get email
   * @return email
  */
  @NotNull @jakarta.validation.constraints.Email
  @JsonProperty("email")
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public UserResponse createdTimestamp(OffsetDateTime createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
    return this;
  }

  /**
   * Get createdTimestamp
   * @return createdTimestamp
  */
  @NotNull @Valid 
  @JsonProperty("createdTimestamp")
  public OffsetDateTime getCreatedTimestamp() {
    return createdTimestamp;
  }

  public void setCreatedTimestamp(OffsetDateTime createdTimestamp) {
    this.createdTimestamp = createdTimestamp;
  }

  public UserResponse updatedTimestamp(OffsetDateTime updatedTimestamp) {
    this.updatedTimestamp = updatedTimestamp;
    return this;
  }

  /**
   * Get updatedTimestamp
   * @return updatedTimestamp
  */
  @NotNull @Valid 
  @JsonProperty("updatedTimestamp")
  public OffsetDateTime getUpdatedTimestamp() {
    return updatedTimestamp;
  }

  public void setUpdatedTimestamp(OffsetDateTime updatedTimestamp) {
    this.updatedTimestamp = updatedTimestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UserResponse userResponse = (UserResponse) o;
    return Objects.equals(this.id, userResponse.id) &&
        Objects.equals(this.name, userResponse.name) &&
        Objects.equals(this.address, userResponse.address) &&
        Objects.equals(this.phoneNumber, userResponse.phoneNumber) &&
        Objects.equals(this.email, userResponse.email) &&
        Objects.equals(this.createdTimestamp, userResponse.createdTimestamp) &&
        Objects.equals(this.updatedTimestamp, userResponse.updatedTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, address, phoneNumber, email, createdTimestamp, updatedTimestamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UserResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    phoneNumber: ").append(toIndentedString(phoneNumber)).append("\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    createdTimestamp: ").append(toIndentedString(createdTimestamp)).append("\n");
    sb.append("    updatedTimestamp: ").append(toIndentedString(updatedTimestamp)).append("\n");
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

