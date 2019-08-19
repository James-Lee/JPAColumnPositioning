package com.javateam.JPAColumnPositioning.entity;

import lombok.Data;

@Data
public class ColumnDefinition {

    private String columnName;

    private String definition;

    private String columnDefinition;

    private int position = Integer.MAX_VALUE - 10;

    public ColumnDefinition(String columnDefinition) {
        try {
            this.columnDefinition = columnDefinition;

            if (columnDefinition.toLowerCase().startsWith("primary key")) {
                position = Integer.MAX_VALUE;
            } else {
                this.columnName = columnDefinition.split(" ")[0];
                this.definition = columnDefinition.split(" ")[1];
            }
        } catch (Exception e) {
            // ignore
        }
    }
   
}