package com.documentprocessing.service;

import com.documentprocessing.models.Response;
import com.documentprocessing.models.ResponseDataList;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.automl.v1.*;
import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.*;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

import static com.documentprocessing.constants.Constants.*;

@Service
@Slf4j
public class DocumentProcessingService {
    public String fullDocumentProcessing(String filePath) throws IOException {
        String response = null;
        Document document = performOcrOnDocument(filePath);
        String predictedModel = classify(document.getText());
        switch (predictedModel) {
            case INSURANCE_MODEL_NAME:
                response= predict(INSURANCE_MODEL_ID, document);
                break;
            case FCA_MODEL_NAME:
                log.info("In Case2");
                break;
        }
        return response;
    }

    public Document performOcrOnDocument(String filePath) throws IOException {

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {
            String name = String.format("projects/%s/locations/%s/processors/%s", PROJECT_ID, LOCATION_US, OCR_PROCESSOR_ID);

            // Read the file.
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));

            // Convert the image data to a Buffer and base64 encode it.
            ByteString content = ByteString.copyFrom(fileData);

            RawDocument rawDocument = RawDocument.newBuilder().setContent(content).setMimeType("application/pdf").build();

            // Configure the process request.
            ProcessRequest request = ProcessRequest.newBuilder().setName(name).setRawDocument(rawDocument).build();

            // Recognizes text entities in the PDF document
            ProcessResponse result = client.processDocument(request);
            Document document = result.getDocument();

            return document;
        }
    }

    private String classify(String content) throws IOException {
        try (PredictionServiceClient client = PredictionServiceClient.create()) {
            // Get the full path of the model.
            ModelName name = ModelName.of(PROJECT_ID, "us-central1", CLASSIFY_MODEL_ID);

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

    private static String predict(String model_ID, Document document) throws IOException {
        String jsonResponse = null;
        try (PredictionServiceClient client = PredictionServiceClient.create()) {
            // Get the full path of the model.
            ModelName name = ModelName.of(PROJECT_ID, LOCATION_CENTRAL_US, model_ID);

            TextSnippet textSnippet =
                    TextSnippet.newBuilder()
                            .setContent(document.getText())
                            .setMimeType("text/plain")
                            .build();
            ExamplePayload payload = ExamplePayload.newBuilder().setTextSnippet(textSnippet).build();
            PredictRequest predictRequest =
                    PredictRequest.newBuilder().setName(name.toString()).setPayload(payload).build();

            PredictResponse response = client.predict(predictRequest);
            List<Response> responseList = new ArrayList<>();
            ResponseDataList responseDataList = new ResponseDataList();
            Map<Integer, Map<Integer, String>> pageLineTextMap = setPageNumberAndLineNumberMap(document, document.getText());
            for (AnnotationPayload annotationPayload : response.getPayloadList()) {
                Response responsePayload = new Response();
                responsePayload.setAttributeName(annotationPayload.getDisplayName());
                responsePayload.setAttributeValue(annotationPayload.getTextExtraction().getTextSegment().getContent());
                responsePayload.setAttributeScore(Double.parseDouble(String.valueOf(annotationPayload.getTextExtraction().getScore())));
                for (Map.Entry<Integer, Map<Integer, String>> page : pageLineTextMap.entrySet()) {
                    for (Map.Entry<Integer, String> lineMap : page.getValue().entrySet()) {
                        String line = lineMap.getValue();
                        if (line.contains(annotationPayload.getDisplayName().replace("_", " ").toUpperCase(Locale.ROOT))) {
                            responsePayload.setLineNumber(String.valueOf(lineMap.getKey()));
                            responsePayload.setPageNumber(String.valueOf(page.getKey()));
                            break;
                        }
                    }
                }
                responseList.add(responsePayload);
            }
            responseDataList.setResponse(responseList);

            ObjectMapper mapper = new ObjectMapper();
            jsonResponse = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDataList);
            System.out.println(jsonResponse);
        }
        return jsonResponse;
    }


    private static Map<Integer, Map<Integer, String>> setPageNumberAndLineNumberMap(Document document, String text) {
        Map<Integer, Map<Integer, String>> pageLineTextMap = new ConcurrentHashMap<>();
        document.getPagesList().parallelStream().forEach(new Consumer<Document.Page>() {
            @Override
            public void accept(Document.Page page) {
                List<Document.Page.Line> linesList = page.getLinesList();
                final int[] lineNum = {0};
                Map<Integer, String> lineNumTextMap = new ConcurrentHashMap<>();
                linesList.stream().forEach(new Consumer<Document.Page.Line>() {
                    @Override
                    public void accept(Document.Page.Line line) {
                        String lineText = getText(line.getLayout(), text);

                        if (lineText != null) {
                            lineNumTextMap.put(++lineNum[0], lineText);
                        }
                    }
                });
                pageLineTextMap.put(page.getPageNumber(), lineNumTextMap);
            }
        });
        return pageLineTextMap;
    }

    private static String getText(Document.Page.Layout layout, String text) {
        Document.TextAnchor textAnchor = layout.getTextAnchor();
        if (!textAnchor.getTextSegmentsList().isEmpty()) {
            int startIndex = 0;
            int endIndex = 0;
            for (Document.TextAnchor.TextSegment textSegment : textAnchor.getTextSegmentsList()) {
                startIndex = (int) textSegment.getStartIndex();
                endIndex = (int) textSegment.getEndIndex();
            }
            return text.substring(startIndex, endIndex);
        }
        return "";
    }
}

