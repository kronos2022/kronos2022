package com.documentprocessing.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.automl.v1.*;
import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.*;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ClassificationService {

    public void classifyDocument(String projectId, String location, String processorId, String filePath, String modelId) throws IOException {
        ClassificationService classService=new ClassificationService();
        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {
            String name = String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            // Read the file.
            byte[] imageFileData = Files.readAllBytes(Paths.get(filePath));

            // Convert the image data to a Buffer and base64 encode it.
            ByteString content = ByteString.copyFrom(imageFileData);

            RawDocument document = RawDocument.newBuilder().setContent(content).setMimeType("application/pdf").build();

            // Configure the process request.
            ProcessRequest request = ProcessRequest.newBuilder().setName(name).setRawDocument(document).build();

            // Recognizes text entities in the PDF document
            ProcessResponse result = client.processDocument(request);
            Document documentResponse = result.getDocument();

            // Get all of the document text as one big string
            String text = documentResponse.getText();

            // Read the text recognition output from the processor
            System.out.println("Full text: " + text);

            predict(projectId,modelId,text);

        }
    }

    public static void predict(String projectId, String modelId, String content) throws IOException {
        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        try (PredictionServiceClient client = PredictionServiceClient.create()) {
            // Get the full path of the model.
            ModelName name = ModelName.of("986902590128", "us-central1", "TEN8598636127022219264");

            // For available mime types, see:
            // https://cloud.google.com/automl/docs/reference/rest/v1/projects.locations.models/predict#textsnippet
            TextSnippet textSnippet = TextSnippet.newBuilder().setContent(content).setMimeType("text/plain") // Types: text/plain, text/html
                    .build();
            ExamplePayload payload = ExamplePayload.newBuilder().setTextSnippet(textSnippet).build();
            PredictRequest predictRequest = PredictRequest.newBuilder().setName(name.toString()).setPayload(payload).build();
            System.out.println(predictRequest);

            PredictResponse response = client.predict(predictRequest);

            for (AnnotationPayload annotationPayload : response.getPayloadList()) {
                System.out.format("Predicted class name: %s\n", annotationPayload.getDisplayName());
                System.out.format("Predicted sentiment score: %.2f\n\n", annotationPayload.getClassification().getScore());
            }
        }
    }
}

