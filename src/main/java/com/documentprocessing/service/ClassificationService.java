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

@Service
@Slf4j
public class ClassificationService {

    public void classifyDocument(String projectId, String location, String processorId, String filePath, String modelId) throws IOException {

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {

            String name =
                    String.format("projects/%s/locations/%s/processors/%s", projectId, location, processorId);

            byte[] pdfFileData = Files.readAllBytes(Paths.get(filePath));

            ByteString content = ByteString.copyFrom(pdfFileData);

            RawDocument document =
                    RawDocument.newBuilder().setContent(content).setMimeType("application/pdf").build();

            ProcessRequest request =
                    ProcessRequest.newBuilder().setName(name).setRawDocument(document).build();

            ProcessResponse result = client.processDocument(request);
            Document documentResponse = result.getDocument();

            String text = documentResponse.getText();

            log.info("Document Text" + text);
            try (PredictionServiceClient predictionServiceClient = PredictionServiceClient.create()) {
                ModelName modelName = ModelName.of(projectId, "us-central1", modelId);
                TextSnippet textSnippet =
                        TextSnippet.newBuilder().setContent(text)
                                .setMimeType("text/plain")
                                .build();
                ExamplePayload payload = ExamplePayload.newBuilder().setTextSnippet(textSnippet).build();
                PredictRequest predictRequest = PredictRequest.newBuilder().setName(name.toString()).setPayload(payload).build();
                PredictResponse response = predictionServiceClient.predict(predictRequest);

                HashMap<String, Float> labelMap = new HashMap<>();
                String predictedLabel = "NONE";
                for (AnnotationPayload annotationPayload : response.getPayloadList()) {
                    labelMap.put(annotationPayload.getDisplayName(), annotationPayload.getClassification().getScore());
                }
                float maxScore = Collections.max(labelMap.values());
                for (Map.Entry<String, Float> entry : labelMap.entrySet()) {
                    if (entry.getValue() == maxScore) {
                        predictedLabel = entry.getKey();
                    }
                }
                log.info(("Predicted Label: " + predictedLabel));
                switch (predictedLabel) {
                    case "Case1":
                        log.info("In Case1");
                        break;
                    case "Case2":
                        log.info("In Case2");
                        break;
                }
            }
        }
    }
}
