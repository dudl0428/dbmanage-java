package com.dbmanage.api.dto;

import javax.validation.constraints.NotBlank;
import java.util.Map;

/**
 * SQL格式化请求DTO
 */
public class SqlFormatRequest {

    /**
     * SQL语句，必填
     */
    @NotBlank(message = "SQL不能为空")
    private String sql;

    /**
     * SQL方言，可选，默认为'sql'
     */
    private String dialect;

    /**
     * 格式化选项，可选
     */
    private Map<String, Object> options;

    // 构造函数
    public SqlFormatRequest() {
    }

    public SqlFormatRequest(String sql, String dialect, Map<String, Object> options) {
        this.sql = sql;
        this.dialect = dialect;
        this.options = options;
    }

    // Getter和Setter
    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDialect() {
        return dialect;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    public Map<String, Object> getOptions() {
        return options;
    }

    public void setOptions(Map<String, Object> options) {
        this.options = options;
    }
} 