package com.eaglebank.api.generated.model;

import java.net.URI;
import java.util.Objects;
import com.eaglebank.api.generated.model.TransactionResponse;
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
 * ListTransactionsResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-07-06T13:05:12.066616+01:00[Europe/London]")
public class ListTransactionsResponse {

  @Valid
  private List<@Valid TransactionResponse> transactions = new ArrayList<>();

  public ListTransactionsResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ListTransactionsResponse(List<@Valid TransactionResponse> transactions) {
    this.transactions = transactions;
  }

  public ListTransactionsResponse transactions(List<@Valid TransactionResponse> transactions) {
    this.transactions = transactions;
    return this;
  }

  public ListTransactionsResponse addTransactionsItem(TransactionResponse transactionsItem) {
    if (this.transactions == null) {
      this.transactions = new ArrayList<>();
    }
    this.transactions.add(transactionsItem);
    return this;
  }

  /**
   * Get transactions
   * @return transactions
  */
  @NotNull @Valid 
  @JsonProperty("transactions")
  public List<@Valid TransactionResponse> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<@Valid TransactionResponse> transactions) {
    this.transactions = transactions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ListTransactionsResponse listTransactionsResponse = (ListTransactionsResponse) o;
    return Objects.equals(this.transactions, listTransactionsResponse.transactions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ListTransactionsResponse {\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
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

