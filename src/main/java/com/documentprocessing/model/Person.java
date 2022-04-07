package com.documentprocessing.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class Person {
    private String name;
    private String designation;
    private String status;
    private String nationality;
    private String dateOfBirth;
    private Double confidenceScore;
    private Long pageNumber;
    private Long lineNumber;
}
