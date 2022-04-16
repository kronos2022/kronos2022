package com.documentprocessing.models.database;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class StoredDataModel {
    private String value;
    private String score;
    private String lineNumber;
    private String pageNumber;
}
