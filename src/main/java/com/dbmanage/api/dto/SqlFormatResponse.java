package com.dbmanage.api.dto;

/**
 * SQL格式化响应DTO
 */
public class SqlFormatResponse {

    /**
     * 格式化后的SQL
     */
    private String formattedSql;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误信息，格式化失败时有值
     */
    private String errorMessage;

    // 构造函数
    public SqlFormatResponse() {
        this.success = true;
    }

    public SqlFormatResponse(String formattedSql) {
        this.formattedSql = formattedSql;
        this.success = true;
    }

    public SqlFormatResponse(String errorMessage, boolean success) {
        this.errorMessage = errorMessage;
        this.success = success;
    }

    public static SqlFormatResponse success(String formattedSql) {
        return new SqlFormatResponse(formattedSql);
    }

    public static SqlFormatResponse error(String errorMessage) {
        return new SqlFormatResponse(errorMessage, false);
    }

    // Getter和Setter
    public String getFormattedSql() {
        return formattedSql;
    }

    public void setFormattedSql(String formattedSql) {
        this.formattedSql = formattedSql;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 