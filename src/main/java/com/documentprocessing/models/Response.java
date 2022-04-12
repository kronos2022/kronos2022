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

}