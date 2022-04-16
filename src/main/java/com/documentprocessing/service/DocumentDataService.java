package com.documentprocessing.service;

import com.documentprocessing.models.ResponseDataList;
import com.documentprocessing.models.database.FCAModel;
import com.documentprocessing.models.database.Person;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.DataSnapshot;
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

@Slf4j
@Service
@DependsOn("firebaseInitializer")
public class DocumentDataService {

    private final String COMPANY_HOUSE_COLLECTION = "company-house";
    private final String FCA_COLLECTION = "fca";
    private final Firestore fireStore = FirestoreClient.getFirestore();

    public void saveDocument(final List<Person> persons) {
        final CollectionReference collectionReference = fireStore.collection(COMPANY_HOUSE_COLLECTION);

        persons.forEach(person -> {
            try {
                String updateTime = updatePerson(collectionReference, person);
                log.debug("Person {} updated at time {}", person, updateTime);
            } catch (Exception e) {
                log.error("Unable to update record : {} \n Following Exception occurred : {}", person, e);
            }
        });

    }

    private String updatePerson(CollectionReference collectionReference, Person person) throws Exception {
        final ApiFuture<WriteResult> apiFuture = collectionReference.document().create(person);
        return apiFuture.get().getUpdateTime().toString();
    }

    public List<Person> getDocumentsByNameAndStatus(final String name, final String status) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(COMPANY_HOUSE_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = collectionReference
                .whereEqualTo("name", name)
                .whereEqualTo("designation", status).get();
        final List<Person> persons = new ArrayList<>();
        for (DocumentSnapshot documentSnapshot : querySnapshot.get().getDocuments()) {
            persons.add(documentSnapshot.toObject(Person.class));
            log.info("Verifying {}", documentSnapshot.get("name"));
        }
        return persons;
    }

    public List<Person> getAllDocuments() throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(COMPANY_HOUSE_COLLECTION);
        final List<Person> persons = new ArrayList<>();
        for (DocumentSnapshot documentSnapshot : collectionReference.get().get().getDocuments()) {
            persons.add(documentSnapshot.toObject(Person.class));
            log.info("Verifying {}", documentSnapshot.get("name"));
        }
        return persons;
    }

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
            log.info("FCA Model Created Successfully : {} ", apiFuture.get().getUpdateTime());
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
                default:
                    log.info("Unrecognized Attribute : {}", map);
                    break;
            }
        });
        fcaModel.setTimeStamp(System.currentTimeMillis());
        log.debug("Constructed FCA Model : {}", fcaModel);

        return fcaModel;
    }
}