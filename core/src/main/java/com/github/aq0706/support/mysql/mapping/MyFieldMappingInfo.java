package com.github.aq0706.support.mysql.mapping;

import java.util.List;

/**
 * @author lidq
 */
public class MyFieldMappingInfo {

    public String tableName;
    public String schema;
    public List<Column> columns;

    public static class Column {
        public String fieldName;
        public String mySQLFieldName;

        public boolean isKey;
        public boolean isAutoIncrement;

        public Column(String fieldName, String mySQLFieldName, boolean isKey, boolean isAutoIncrement) {
            this.fieldName = fieldName;
            this.mySQLFieldName = mySQLFieldName;
            this.isKey = isKey;
            this.isAutoIncrement = isAutoIncrement;
        }
    }
}