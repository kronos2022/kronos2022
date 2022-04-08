package com.documentprocessing.service;

import com.documentprocessing.model.Person;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    public List<Person> getDocumentsByNameAndStatus(final String name, final String status) throws ExecutionException, InterruptedException {
        final CollectionReference collectionReference = fireStore.collection(COLLECTION);
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
        final CollectionReference collectionReference = fireStore.collection(COLLECTION);
        final List<Person> persons = new ArrayList<>();
        for (DocumentSnapshot documentSnapshot : collectionReference.get().get().getDocuments()) {
            persons.add(documentSnapshot.toObject(Person.class));
            log.info("Verifying {}", documentSnapshot.get("name"));
        }
        return persons;
    }
}