package com.documentprocessing.models;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class Response {
    private String attributeName;
    private String attributeValue;
    private double attributeScore;
    private String pageNumber;
    private String lineNumber;

    @Override
    public String toString() {
        char value='\u0022';
        return "{"+value+attributeName+value+":" + value+attributeValue +value+
                ","+value+ "Confidence Score"+ value+":"+ value+ attributeScore + value+
                ","+value+"Page Number"+ value+":"+ value+pageNumber +value+
                ","+value+ "Line Number"+value+":"+  value+ lineNumber +value+
                "}";
    }
}