package com.documentprocessing.service;

import com.documentprocessing.models.Response;
import com.documentprocessing.models.ResponseDataList;
import com.google.cloud.automl.v1.*;
import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import static com.documentprocessing.constants.Constants.*;

@Service
@Slf4j
public class DocumentProcessingService {

    @Autowired
    private FileService fileService;
    @Autowired
    private DocumentDataService documentDataService;

    public String fullDocumentProcessing(MultipartFile file) throws IOException {
        String url = fileService.upload(file);
        ResponseDataList response = new ResponseDataList();
        Document document = performOcrOnDocument(url);
        String predictedModel = classify(document.getText());
        switch (predictedModel) {
            case INSURANCE_MODEL_NAME:
                response = predict(INSURANCE_MODEL_ID, document, INSURANCE_MODEL_NAME);
                break;
            case FCA_MODEL_NAME:
                response = predict(FCA_MODEL_ID, document, FCA_MODEL_NAME);
                try {
                    documentDataService.saveFcaClassification(response);
                } catch (Exception e) {
                    log.error("****====== FCA Response Save Error ======**** \n {}", e.getMessage());
                }
                break;
        }
        return response.toString();
    }

    public Document performOcrOnDocument(String filePath) throws IOException {

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create()) {
            String name = String.format("projects/%s/locations/%s/processors/%s", PROJECT_ID, LOCATION_US, OCR_PROCESSOR_ID);

            // Read the file.
            URL url = new URL(filePath);
            InputStream in = url.openStream();
            byte[] fileData = in.readAllBytes();

            // Convert the image data to a Buffer and base64 encode it.
            ByteString content = ByteString.copyFrom(fileData);

            RawDocument rawDocument = RawDocument.newBuilder().setContent(content).setMimeType("application/pdf").build();

            // Configure the process request.
            ProcessRequest request = ProcessRequest.newBuilder().setName(name).setRawDocument(rawDocument).build();

            // Recognizes text entities in the PDF document
            ProcessResponse result = client.processDocument(request);

            return result.getDocument();
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

            HashMap<String, Float> labelMap = new HashMap<>();
            String predictedModel = "NONE";
            for (AnnotationPayload annotationPayload : response.getPayloadList()) {
                System.out.format("Predicted class name: %s\n", annotationPayload.getDisplayName());
                System.out.format("Predicted sentiment score: %.2f\n\n", annotationPayload.getClassification().getScore());
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

    private static ResponseDataList predict(String model_ID, Document document, String model_name) throws IOException {
        System.out.println(document.getText());
        String jsonResponse = null;
        ResponseDataList responseDataList = new ResponseDataList();
        final List<Response> responseList = new ArrayList<>();
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
            Response responsePayload;
            int fcaUrlCount = 0;

            Map<Integer, Map<Integer, String>> pageLineTextMap = setPageNumberAndLineNumberMap(document, document.getText());
            System.out.println(response.getPayloadList());

            for (AnnotationPayload annotationPayload : response.getPayloadList()) {
                String annotationDisplayName = annotationPayload.getDisplayName();
                if (!annotationPayload.getDisplayName().contains("Consumer_Helpline")) {
                    if (!annotationPayload.getDisplayName().contains("Footer")) {
                        responsePayload = new Response();

                        responsePayload.setAttributeName(annotationDisplayName);
                        responsePayload.setAttributeValue(annotationPayload.getTextExtraction().getTextSegment().getContent().replaceAll("\n", ""));
                        responsePayload.setAttributeScore(Double.parseDouble(String.valueOf(annotationPayload.getTextExtraction().getScore())));

                        for (Map.Entry<Integer, Map<Integer, String>> page : pageLineTextMap.entrySet()) {
                            for (Map.Entry<Integer, String> lineMap : page.getValue().entrySet()) {
                                String line = lineMap.getValue();
                                if (model_name.equals(INSURANCE_MODEL_NAME)) {
                                    if (line.contains(annotationDisplayName.replace("_", " ").toUpperCase(Locale.ROOT))) {
                                        responsePayload.setLineNumber(String.valueOf(lineMap.getKey()));
                                        responsePayload.setPageNumber(String.valueOf(page.getKey()));
                                        break;
                                    }
                                } else if (model_name.equals(FCA_MODEL_NAME)) {
                                    if (line.contains(annotationPayload.getTextExtraction().getTextSegment().getContent())) {
                                        responsePayload.setLineNumber(String.valueOf(lineMap.getKey()));
                                        responsePayload.setPageNumber(String.valueOf(page.getKey()));
                                        break;
                                    }
                                }

                            }
                        }

                        responseList.add(responsePayload);
                    }
                }
            }
            log.info("Response List : {}", responseList);
            responseDataList.setResponse(responseList);

        }
        return responseDataList;
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

