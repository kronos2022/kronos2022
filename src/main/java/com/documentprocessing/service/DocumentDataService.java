package com.documentprocessing.service;

import com.documentprocessing.model.Person;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@DependsOn("firebaseInitializer")
public class DocumentDataService {

    private final String COLLECTION = "company-house";
    private final Firestore fireStore = FirestoreClient.getFirestore();

    public void saveDocument(final List<Person> persons) {
        final CollectionReference collectionReference = fireStore.collection(COLLECTION);

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
}