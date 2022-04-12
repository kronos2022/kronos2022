package com.documentprocessing.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FileService {

    private final Bucket bucket = StorageClient.getInstance().bucket();

    public String upload(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();                        // to get original file name
        assert fileName != null;
        Path path = this.writeToTempFile(multipartFile);                      // to convert multipartFile to File
        String TEMP_URL = this.uploadFile(path, fileName, multipartFile.getContentType()); // to get uploaded file link
        assert Files.deleteIfExists(path);
        return TEMP_URL;                                                              // Your customized response

    }

    public String download(String fileName) throws IOException {
        Credentials credentials = GoogleCredentials.getApplicationDefault();
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        Blob blob = storage.get(BlobId.of("psyched-signal-345109.appspot.com", fileName));
        URL downloadURL = blob.signUrl(1, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        return downloadURL.toString();
    }

    private String uploadFile(Path path, String fileName, String contentType) throws IOException {
        BlobId blobId = BlobId.of("psyched-signal-345109.appspot.com", fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        Credentials credentials = GoogleCredentials.getApplicationDefault();
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        Blob blob = storage.create(blobInfo, Files.readAllBytes(path));
        URL downloadURL = blob.signUrl(1, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        log.debug("Sign Url: {}", downloadURL);
        return downloadURL.toString();
    }

    private Path writeToTempFile(MultipartFile multipartFile) throws IOException {

        Path path = Files.createTempFile(null, null);
        log.info("Temp File path: {}", path);
        Files.write(path, multipartFile.getBytes());
        return path;
    }

    public void someMethod() {
        try {

            // create a temporary file
            Path tempFile = Files.createTempFile(null, null);
            System.out.println(tempFile);

            // write a line
            Files.write(tempFile, "Hello World\n".getBytes(StandardCharsets.UTF_8));

            // append a list of lines, add new lines automatically
            List<String> content = Arrays.asList("Line 1", "Line 2", "Line 3");
            Files.write(tempFile, content, StandardOpenOption.APPEND);

            assert Files.deleteIfExists(tempFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
