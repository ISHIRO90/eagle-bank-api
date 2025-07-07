package com.eaglebank.api.generated.model;

import java.net.URI;
import java.util.Objects;
import com.eaglebank.api.generated.model.BankAccountResponse;
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
 * ListBankAccountsResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-07-06T13:05:12.066616+01:00[Europe/London]")
public class ListBankAccountsResponse {

  @Valid
  private List<@Valid BankAccountResponse> accounts = new ArrayList<>();

  public ListBankAccountsResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ListBankAccountsResponse(List<@Valid BankAccountResponse> accounts) {
    this.accounts = accounts;
  }

  public ListBankAccountsResponse accounts(List<@Valid BankAccountResponse> accounts) {
    this.accounts = accounts;
    return this;
  }

  public ListBankAccountsResponse addAccountsItem(BankAccountResponse accountsItem) {
    if (this.accounts == null) {
      this.accounts = new ArrayList<>();
    }
    this.accounts.add(accountsItem);
    return this;
  }

  /**
   * Get accounts
   * @return accounts
  */
  @NotNull @Valid 
  @JsonProperty("accounts")
  public List<@Valid BankAccountResponse> getAccounts() {
    return accounts;
  }

  public void setAccounts(List<@Valid BankAccountResponse> accounts) {
    this.accounts = accounts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ListBankAccountsResponse listBankAccountsResponse = (ListBankAccountsResponse) o;
    return Objects.equals(this.accounts, listBankAccountsResponse.accounts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accounts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ListBankAccountsResponse {\n");
    sb.append("    accounts: ").append(toIndentedString(accounts)).append("\n");
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

