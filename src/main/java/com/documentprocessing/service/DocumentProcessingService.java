package com.documentprocessing.service;

import com.google.cloud.automl.v1.*;
import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.documentprocessing.constants.Constants.*;

@Service
@Slf4j
public class DocumentProcessingService {
    public void fullDocumentProcessing(String filePath) throws IOException {
        String text = performOcrOnDocument(filePath);
        String predictedModel = classify(text);
        switch (predictedModel) {
            case INSURANCE_MODEL_NAME:
                predict(INSURANCE_MODEL_ID,text);
                break;
            case FCA_MODEL_NAME:
                log.info("In Case2");
                break;
        }
    }

    public String performOcrOnDocument(String filePath) throws IOException {

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {
            String name = String.format("projects/%s/locations/%s/processors/%s",PROJECT_ID,LOCATION_US,OCR_PROCESSOR_ID);

            // Read the file.
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));

            // Convert the image data to a Buffer and base64 encode it.
            ByteString content = ByteString.copyFrom(fileData);

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

            return text;

        }
    }

    private String classify(String content) throws IOException {
        try (PredictionServiceClient client = PredictionServiceClient.create()) {
            // Get the full path of the model.
            ModelName name = ModelName.of(PROJECT_ID, "us-central1",CLASSIFY_MODEL_ID );

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

            HashMap<String, Float> labelMap = new HashMap<>();
            String predictedModel = "NONE";
            for (AnnotationPayload annotationPayload : response.getPayloadList()) {
                labelMap.put(annotationPayload.getDisplayName(), annotationPayload.getClassification().getScore());
            }
            float maxScore = Collections.max(labelMap.values());
            for (Map.Entry<String, Float> entry : labelMap.entrySet()) {
                if (entry.getValue() == maxScore) {
                    predictedModel = entry.getKey();
                }
            }
            log.info(("Predicted Label: " + predictedModel));

        return predictedModel;
        }
    }

    private static void predict(String model_ID,String content) throws IOException {

        try (PredictionServiceClient client = PredictionServiceClient.create()) {
            // Get the full path of the model.
            ModelName name = ModelName.of(PROJECT_ID, LOCATION_CENTRAL_US, model_ID);

            TextSnippet textSnippet =
                    TextSnippet.newBuilder()
                            .setContent(content)
                            .setMimeType("text/plain")
                            .build();
            ExamplePayload payload = ExamplePayload.newBuilder().setTextSnippet(textSnippet).build();
            PredictRequest predictRequest =
                    PredictRequest.newBuilder().setName(name.toString()).setPayload(payload).build();

            PredictResponse response = client.predict(predictRequest);

            for (AnnotationPayload annotationPayload : response.getPayloadList()) {
                System.out.format("Text Extract Entity Type: %s\n", annotationPayload.getDisplayName());
                System.out.format("Text score: %.2f\n", annotationPayload.getTextExtraction().getScore());
                TextSegment textSegment = annotationPayload.getTextExtraction().getTextSegment();
                System.out.format("Text Extract Entity Content: %s\n", textSegment.getContent());
                System.out.format("Text Start Offset: %s\n", textSegment.getStartOffset());
                System.out.format("Text End Offset: %s\n\n", textSegment.getEndOffset());
            }
        }
    }
}

