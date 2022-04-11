package com.documentprocessing.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FileService {

    private final Bucket bucket = StorageClient.getInstance().bucket();

    public String upload(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();                        // to get original file name
        assert fileName != null;
        String fileNameWithExtension = fileName.concat(this.getExtension(fileName));
        File file = this.convertToFile(multipartFile, fileNameWithExtension);                      // to convert multipartFile to File
        String TEMP_URL = this.uploadFile(file, fileName, multipartFile.getContentType());                                   // to get uploaded file link
        boolean delete = file.delete();                                               // to delete the copy of uploaded file stored in the project folder
        assert delete;
        return TEMP_URL;                                                              // Your customized response

    }

    public String download(String fileName) throws IOException {
        Credentials credentials = GoogleCredentials.getApplicationDefault();
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        Blob blob = storage.get(BlobId.of("psyched-signal-345109.appspot.com", fileName));
        URL downloadURL = blob.signUrl(1, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        return downloadURL.toString();
    }

    private String uploadFile(File file, String fileName, String contentType) throws IOException {
        BlobId blobId = BlobId.of("psyched-signal-345109.appspot.com", fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(contentType).build();
        Credentials credentials = GoogleCredentials.getApplicationDefault();
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        Blob blob = storage.create(blobInfo, Files.readAllBytes(file.toPath()));
        URL downloadURL = blob.signUrl(1, TimeUnit.MINUTES, Storage.SignUrlOption.withV4Signature());
        log.debug("Sign Url: {}", downloadURL);
        return downloadURL.toString();
    }

    private File convertToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File tempFile = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(multipartFile.getBytes());
        }
        return tempFile;
    }

    private String getExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
