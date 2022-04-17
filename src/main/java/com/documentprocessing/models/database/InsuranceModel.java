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
public class InsuranceModel {
    private Map<String, String> policyNumber;
    private Map<String, String> customerId;
    private Map<String, String> customerName;
    private Map<String, String> insuranceType;
    private Map<String, String> premiumAmount;
    private Map<String, String> claimAmount;
    private Map<String, String> postalCode;
    private Map<String, String> age;
    private Map<String, String> riskSegmentation;
    private Map<String, String> claimStatus;
    private Map<String, String> incidentSeverity;
    private final List<Map<String, String>> otherAttributes = new ArrayList<>();
    private Long timeStamp;
}
