package com.documentprocessing.models.database;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FCAModel {
    private Map<String, String> companyName;
    private final List<Map<String, String>> addresses = new ArrayList<>();
    private final List<Map<String, String>> contactNumbers = new ArrayList<>();
    private final List<Map<String, String>> emails = new ArrayList<>();
    private final List<Map<String, String>> fax = new ArrayList<>();
    private final List<Map<String, String>> websites = new ArrayList<>();
    private final List<Map<String, String>> otherAttributes = new ArrayList<>();
    private Long timeStamp;
}
