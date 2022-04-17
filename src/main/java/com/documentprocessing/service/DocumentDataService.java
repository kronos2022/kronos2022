package com.documentprocessing.service;

import com.documentprocessing.models.ResponseDataList;
import com.documentprocessing.models.database.FCAModel;
import com.documentprocessing.models.database.InsuranceModel;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.documentprocessing.constants.AttributeConstants.*;
import static com.documentprocessing.constants.FcaAttributes.*;
import static com.documentprocessing.constants.InsuranceAttributes.*;

@Slf4j
@Service
@DependsOn("firebaseInitializer")
public class DocumentDataService {

    private final String FCA_COLLECTION = "fca";
    private final String INSURANCE_COLLECTION = "insurance-details";
    private final Firestore fireStore = FirestoreClient.getFirestore();

    public void saveFcaClassification(final ResponseDataList responseDataList) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(FCA_COLLECTION);
        final FCAModel fcaModel = constructFCAStorageModel(responseDataList);

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("companyName.value", fcaModel.getCompanyName().get(ATTRIBUTE_VALUE))
                .get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                final ApiFuture<WriteResult> apiFuture = collectionReference.document(snapshot.getId()).set(fcaModel);
                log.info("FCA Model Updated Successfully : {} ", apiFuture.get().getUpdateTime());
            }
        } else {
            final ApiFuture<WriteResult> apiFuture = collectionReference.document().create(fcaModel);
            log.info("FCA Model Saved Successfully : {} ", apiFuture.get().getUpdateTime());
        }

    }

    private FCAModel constructFCAStorageModel(final ResponseDataList responseDataList) {
        FCAModel fcaModel = new FCAModel();
        responseDataList.getResponse().forEach(response -> {
            Map<String, String> map = new HashMap<>();
            map.put(ATTRIBUTE_VALUE, response.getAttributeValue());
            map.put(ATTRIBUTE_SCORE, String.valueOf(response.getAttributeScore()));
            map.put(ATTRIBUTE_LINE_NUMBER, response.getLineNumber());
            map.put(ATTRIBUTE_PAGE_NUMBER, response.getPageNumber());
            switch (response.getAttributeName()) {
                case COMPANY_NAME:
                    fcaModel.setCompanyName(map);
                    break;
                case ADDRESS:
                    fcaModel.getAddresses().add(map);
                    break;
                case EMAIL:
                    fcaModel.getEmails().add(map);
                    break;
                case TELEPHONE:
                    fcaModel.getContactNumbers().add(map);
                    break;
                case FAX:
                    fcaModel.getFax().add(map);
                    break;
                case WEBSITE:
                    fcaModel.getWebsites().add(map);
                    break;
                default:
                    map.put(ATTRIBUTE_NAME, response.getAttributeName());
                    log.info("Unrecognized Attribute : {}", map);
                    fcaModel.getOtherAttributes().add(map);
                    break;
            }
        });
        fcaModel.setTimeStamp(System.currentTimeMillis());
        log.debug("Constructed FCA Model : {}", fcaModel);

        return fcaModel;
    }

    public void saveInsuranceClassification(final ResponseDataList responseDataList) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final InsuranceModel insuranceModel = constructInsuranceStorageModel(responseDataList);
        final String documentId = insuranceModel.getPolicyNumber().get(ATTRIBUTE_VALUE);

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo(FieldPath.documentId(), documentId)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                final ApiFuture<WriteResult> apiFuture = collectionReference.document(snapshot.getId()).set(insuranceModel);
                log.info("Insurance Model Updated Successfully : {} ", apiFuture.get().getUpdateTime());
            }
        } else {
            final ApiFuture<WriteResult> apiFuture = collectionReference.document(documentId).create(insuranceModel);
            log.info("Insurance Model Saved Successfully : {} ", apiFuture.get().getUpdateTime());
        }
    }

    private InsuranceModel constructInsuranceStorageModel(final ResponseDataList responseDataList) {
        InsuranceModel insuranceModel = new InsuranceModel();
        responseDataList.getResponse().forEach(response -> {
            Map<String, String> map = new HashMap<>();
            map.put(ATTRIBUTE_VALUE, response.getAttributeValue());
            map.put(ATTRIBUTE_SCORE, String.valueOf(response.getAttributeScore()));
            map.put(ATTRIBUTE_LINE_NUMBER, response.getLineNumber());
            map.put(ATTRIBUTE_PAGE_NUMBER, response.getPageNumber());
            switch (response.getAttributeName()) {
                case CUSTOMER_ID:
                    insuranceModel.setCustomerId(map);
                    break;
                case POLICY_NUMBER:
                    insuranceModel.setPolicyNumber(map);
                    break;
                case INSURANCE_TYPE:
                    insuranceModel.setInsuranceType(map);
                    break;
                case PREMIUM_AMOUNT:
                    insuranceModel.setPremiumAmount(map);
                    break;
                case CLAIM_AMOUNT:
                    insuranceModel.setClaimAmount(map);
                    break;
                case CUSTOMER_NAME:
                    insuranceModel.setCustomerName(map);
                    break;
                case POSTAL_CODE:
                    insuranceModel.setPostalCode(map);
                    break;
                case AGE:
                    insuranceModel.setAge(map);
                    break;
                case RISK_SEGMENTATION:
                    insuranceModel.setRiskSegmentation(map);
                    break;
                case CLAIM_STATUS:
                    insuranceModel.setClaimStatus(map);
                    break;
                case INCIDENT_SEVERITY:
                    insuranceModel.setIncidentSeverity(map);
                    break;
                default:
                    map.put(ATTRIBUTE_NAME, response.getAttributeName());
                    insuranceModel.getOtherAttributes().add(map);
                    log.info("Unrecognized Attribute : {}", map);
                    break;
            }
        });
        insuranceModel.setTimeStamp(System.currentTimeMillis());
        log.debug("Constructed Insurance Model : {}", insuranceModel);

        return insuranceModel;
    }


    // ********************************** Retrieval Methods ****************************** //

    public List<FCAModel> getFcaEntityByCompanyName(final String companyName) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(FCA_COLLECTION);

        List<FCAModel> companies = new ArrayList<>();
        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("companyName.value", companyName)
                .get();

        if (!querySnapshot.get().getDocuments().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                FCAModel fcaModel = snapshot.toObject(FCAModel.class);
                companies.add(fcaModel);
                log.info("FCA model fetched successfully : {} ", fcaModel);
            }
        }

        return companies;
    }

    public List<InsuranceModel> getInsuranceEntityByPolicyNumber(String policyNumber) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final List<InsuranceModel> policies = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo(FieldPath.documentId(), policyNumber)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                InsuranceModel insuranceModel = snapshot.toObject(InsuranceModel.class);
                policies.add(insuranceModel);
                log.info("Insurance Model Fetched Successfully : {} ", insuranceModel);
            }
        }
        return policies;
    }

    public List<InsuranceModel> getInsuranceEntitiesByCustomerId(String customerId) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final List<InsuranceModel> policies = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("customerId.value", customerId)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                InsuranceModel insuranceModel = snapshot.toObject(InsuranceModel.class);
                policies.add(insuranceModel);
                log.info("Insurance Model Fetched Successfully : {} ", insuranceModel);
            }
        }
        return policies;
    }

    public List<InsuranceModel> getInsuranceEntitiesByInsuranceType(String insuranceType) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final List<InsuranceModel> policies = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("insuranceType.value", insuranceType)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                InsuranceModel insuranceModel = snapshot.toObject(InsuranceModel.class);
                policies.add(insuranceModel);
                log.info("Insurance Model Fetched Successfully : {} ", insuranceModel);
            }
        }
        return policies;
    }

    public List<InsuranceModel> getInsuranceEntitiesByIncidentSeverity(String incidentSeverity) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final List<InsuranceModel> policies = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("incidentSeverity.value", incidentSeverity)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                InsuranceModel insuranceModel = snapshot.toObject(InsuranceModel.class);
                policies.add(insuranceModel);
                log.info("Insurance Model Fetched Successfully : {} ", insuranceModel);
            }
        }
        return policies;
    }

    public List<InsuranceModel> getInsuranceEntitiesByRiskSegmentation(String riskSegmentation) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final List<InsuranceModel> policies = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("riskSegmentation.value", riskSegmentation)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                InsuranceModel insuranceModel = snapshot.toObject(InsuranceModel.class);
                policies.add(insuranceModel);
                log.info("Insurance Model Fetched Successfully : {} ", insuranceModel);
            }
        }
        return policies;
    }

    public List<InsuranceModel> getInsuranceEntitiesByCustomerIdAndIncidentSeverity(String customerId, String incidentSeverity) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final List<InsuranceModel> policies = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("customerId.value", customerId)
                .whereEqualTo("incidentSeverity.value", incidentSeverity)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                InsuranceModel insuranceModel = snapshot.toObject(InsuranceModel.class);
                policies.add(insuranceModel);
                log.info("Insurance Model Fetched Successfully : {} ", insuranceModel);
            }
        }
        return policies;
    }

    public List<InsuranceModel> getInsuranceEntitiesByCustomerIdAndInsuranceType(String customerId, String insuranceType) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final List<InsuranceModel> policies = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("customerId.value", customerId)
                .whereEqualTo("insuranceType.value", insuranceType)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                InsuranceModel insuranceModel = snapshot.toObject(InsuranceModel.class);
                policies.add(insuranceModel);
                log.info("Insurance Model Fetched Successfully : {} ", insuranceModel);
            }
        }
        return policies;
    }

    public List<InsuranceModel> getInsuranceEntitiesByCustomerIdAndRiskSegmentation(String customerId, String riskSegmentation) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(INSURANCE_COLLECTION);
        final List<InsuranceModel> policies = new ArrayList<>();

        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("customerId.value", customerId)
                .whereEqualTo("riskSegmentation.value", riskSegmentation)
                .get();

        if (!querySnapshot.get().isEmpty()) {
            for (DocumentSnapshot snapshot : querySnapshot.get().getDocuments()) {
                InsuranceModel insuranceModel = snapshot.toObject(InsuranceModel.class);
                policies.add(insuranceModel);
                log.info("Insurance Model Fetched Successfully : {} ", insuranceModel);
            }
        }
        return policies;
    }

}