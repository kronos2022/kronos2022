package com.documentprocessing.controller;

import com.documentprocessing.models.database.FCAModel;
import com.documentprocessing.models.database.InsuranceModel;
import com.documentprocessing.service.DocumentDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/document-data")
@Slf4j
@SuppressWarnings("unused")
public class DocumentDataController {

    @Autowired
    DocumentDataService documentDataService;

    @GetMapping("/get-fca-entity-by-name")
    private ResponseEntity<List<FCAModel>> getFcaEntityByName(
            @RequestParam final String companyName) throws ExecutionException, InterruptedException {
        List<FCAModel> companies = documentDataService.getFcaEntityByCompanyName(companyName);
        return ResponseEntity.ok().body(companies);
    }

    @GetMapping("/get-insurance-entity-by-policy-number")
    private ResponseEntity<List<InsuranceModel>> getInsuranceEntityByPolicyNumber(
            @RequestParam final String policyNumber) throws ExecutionException, InterruptedException {
        List<InsuranceModel> policies = documentDataService.getInsuranceEntityByPolicyNumber(policyNumber);
        return ResponseEntity.ok().body(policies);
    }

    @GetMapping("/get-insurance-entities-by-customer-id")
    private ResponseEntity<List<InsuranceModel>> getInsuranceEntityByCustomerId(
            @RequestParam final String customerId) throws ExecutionException, InterruptedException {
        List<InsuranceModel> policies = documentDataService.getInsuranceEntitiesByCustomerId(customerId);
        return ResponseEntity.ok().body(policies);
    }

    @GetMapping("/get-insurance-entities-by-insurance-type")
    private ResponseEntity<List<InsuranceModel>> getInsuranceEntityByInsuranceType(
            @RequestParam final String insuranceType) throws ExecutionException, InterruptedException {
        List<InsuranceModel> policies = documentDataService
                .getInsuranceEntitiesByInsuranceType(insuranceType);
        return ResponseEntity.ok().body(policies);
    }

    @GetMapping("/get-insurance-entities-by-incident-severity")
    private ResponseEntity<List<InsuranceModel>> getInsuranceEntityByIncidentSeverity(
            @RequestParam final String incidentSeverity) throws ExecutionException, InterruptedException {
        List<InsuranceModel> policies = documentDataService
                .getInsuranceEntitiesByIncidentSeverity(incidentSeverity);
        return ResponseEntity.ok().body(policies);
    }

    @GetMapping("/get-insurance-entities-by-risk-segmentation")
    private ResponseEntity<List<InsuranceModel>> getInsuranceEntityByRiskSegmentation(
            @RequestParam final String riskSegmentation) throws ExecutionException, InterruptedException {
        List<InsuranceModel> policies = documentDataService
                .getInsuranceEntitiesByRiskSegmentation(riskSegmentation);
        return ResponseEntity.ok().body(policies);
    }

    @GetMapping("/get-insurance-entities-by-customer-id-and-insurance-type")
    private ResponseEntity<List<InsuranceModel>> getInsuranceEntityByCustomerIdAndInsuranceType(
            @RequestParam final String customerId,
            @RequestParam final String insuranceType) throws ExecutionException, InterruptedException {
        List<InsuranceModel> policies = documentDataService
                .getInsuranceEntitiesByCustomerIdAndInsuranceType(customerId, insuranceType);
        return ResponseEntity.ok().body(policies);
    }

    @GetMapping("/get-insurance-entities-by-customer-id-and-incident-severity")
    private ResponseEntity<List<InsuranceModel>> getInsuranceEntityByCustomerIdAndIncidentSeverity(
            @RequestParam final String customerId,
            @RequestParam final String incidentSeverity) throws ExecutionException, InterruptedException {
        List<InsuranceModel> policies = documentDataService
                .getInsuranceEntitiesByCustomerIdAndIncidentSeverity(customerId, incidentSeverity);
        return ResponseEntity.ok().body(policies);
    }

    @GetMapping("/get-insurance-entities-by-customer-id-and-risk-segmentation")
    private ResponseEntity<List<InsuranceModel>> getInsuranceEntityByCustomerIdAndRiskSegmentation(
            @RequestParam final String customerId,
            @RequestParam final String riskSegmentation) throws ExecutionException, InterruptedException {
        List<InsuranceModel> policies = documentDataService
                .getInsuranceEntitiesByCustomerIdAndRiskSegmentation(customerId, riskSegmentation);
        return ResponseEntity.ok().body(policies);
    }
}