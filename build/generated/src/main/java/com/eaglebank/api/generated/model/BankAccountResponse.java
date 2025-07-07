package com.eaglebank.api.generated.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.OffsetDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * BankAccountResponse
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-07-06T13:05:12.066616+01:00[Europe/London]")
public class BankAccountResponse {

  private String accountNumber;

  /**
   * Gets or Sets sortCode
   */
  public enum SortCodeEnum {
    _10_10_10("10-10-10");

    private String value;

    SortCodeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static SortCodeEnum fromValue(String value) {
      for (SortCodeEnum b : SortCodeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private SortCodeEnum sortCode;

  private String name;

  /**
   * Gets or Sets accountType
   */
  public enum AccountTypeEnum {
    PERSONAL("personal");

    private String value;

    AccountTypeEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static AccountTypeEnum fromValue(String value) {
      for (AccountTypeEnum b : AccountTypeEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private AccountTypeEnum accountType;

  private Double balance;

  /**
   * Gets or Sets currency
   */
  public enum CurrencyEnum {
    GBP("GBP");

    private String value;

    CurrencyEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static CurrencyEnum fromValue(String value) {
      for (CurrencyEnum b : CurrencyEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  private CurrencyEnum currency;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime createdTimestamp;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime updatedTimestamp;

  public BankAccountResponse() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public BankAccountResponse(String accountNumber, SortCodeEnum sortCode, String name, AccountTypeEnum accountType, Double balance, CurrencyEnum currency, OffsetDateTime createdTimestamp, OffsetDateTime updatedTimestamp) {
    this.accountNumber = accountNumber;
    this.sortCode = sortCode;
    this.name = name;
    this.accountType = accountType;
    this.balance = balance;
    this.currency = currency;
    this.createdTimestamp = createdTimestamp;
    this.updatedTimestamp = updatedTimestamp;
  }

  public BankAccountResponse accountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
    return this;
  }

  /**
   * Get accountNumber
   * @return accountNumber
  */
  @NotNull 
  @JsonProperty("accountNumber")
  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public BankAccountResponse sortCode(SortCodeEnum sortCode) {
    this.sortCode = sortCode;
    return this;
  }

  /**
   * Get sortCode
   * @return sortCode
  */
  @NotNull 
  @JsonProperty("sortCode")
  public SortCodeEnum getSortCode() {
    return sortCode;
  }

  public void setSortCode(SortCodeEnum sortCode) {
    this.sortCode = sortCode;
  }

  public BankAccountResponse name(String name) {
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

  public BankAccountResponse accountType(AccountTypeEnum accountType) {
    this.accountType = accountType;
    return this;
  }

  /**
   * Get accountType
   * @return accountType
  */
  @NotNull 
  @JsonProperty("accountType")
  public AccountTypeEnum getAccountType() {
    return accountType;
  }

  public void setAccountType(AccountTypeEnum accountType) {
    this.accountType = accountType;
  }

  public BankAccountResponse balance(Double balance) {
    this.balance = balance;
    return this;
  }

  /**
   * Currency amount with up to two decimal places
   * minimum: 0.0
   * maximum: 10000.0
   * @return balance
  */
  @NotNull @DecimalMin("0.0") @DecimalMax("10000.0") 
  @JsonProperty("balance")
  public Double getBalance() {
    return balance;
  }

  public void setBalance(Double balance) {
    this.balance = balance;
  }

  public BankAccountResponse currency(CurrencyEnum currency) {
    this.currency = currency;
    return this;
  }

  /**
   * Get currency
   * @return currency
  */
  @NotNull 
  @JsonProperty("currency")
  public CurrencyEnum getCurrency() {
    return currency;
  }

  public void setCurrency(CurrencyEnum currency) {
    this.currency = currency;
  }

  public BankAccountResponse createdTimestamp(OffsetDateTime createdTimestamp) {
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

  public BankAccountResponse updatedTimestamp(OffsetDateTime updatedTimestamp) {
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
    BankAccountResponse bankAccountResponse = (BankAccountResponse) o;
    return Objects.equals(this.accountNumber, bankAccountResponse.accountNumber) &&
        Objects.equals(this.sortCode, bankAccountResponse.sortCode) &&
        Objects.equals(this.name, bankAccountResponse.name) &&
        Objects.equals(this.accountType, bankAccountResponse.accountType) &&
        Objects.equals(this.balance, bankAccountResponse.balance) &&
        Objects.equals(this.currency, bankAccountResponse.currency) &&
        Objects.equals(this.createdTimestamp, bankAccountResponse.createdTimestamp) &&
        Objects.equals(this.updatedTimestamp, bankAccountResponse.updatedTimestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountNumber, sortCode, name, accountType, balance, currency, createdTimestamp, updatedTimestamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BankAccountResponse {\n");
    sb.append("    accountNumber: ").append(toIndentedString(accountNumber)).append("\n");
    sb.append("    sortCode: ").append(toIndentedString(sortCode)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    accountType: ").append(toIndentedString(accountType)).append("\n");
    sb.append("    balance: ").append(toIndentedString(balance)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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

