package org.example.ihatesupa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api-key}")
    private String supabaseApiKey;

    @Value("${supabase.bucket-name}")
    private String bucketName;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String uploadFile(MultipartFile multipartFile) throws IOException, InterruptedException {
        String boundary = "Boundary-" + UUID.randomUUID();
//        String fileName = UUID.randomUUID() + "-" + multipartFile.getOriginalFilename();
        String fileName = "%s.%s".formatted(UUID.randomUUID(), Objects.requireNonNull(multipartFile.getContentType()).split("/")[1]);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, fileName)))
                .header("Authorization", "Bearer " + supabaseApiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(ofMimeMultipartData(multipartFile, boundary))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
//            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, fileName);
            return fileName;
        } else {
            throw new IOException("Supabase upload error: " + response.body());
        }
    }

    private HttpRequest.BodyPublisher ofMimeMultipartData(MultipartFile file, String boundary) throws IOException {
        List<byte[]> byteArrays = List.of(
                ("--" + boundary + "\r\n" +
                        "Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getOriginalFilename() + "\"\r\n" +
                        "Content-Type: " + file.getContentType() + "\r\n\r\n").getBytes(),
                file.getBytes(),
                ("\r\n--" + boundary + "--\r\n").getBytes()
        );
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }
}