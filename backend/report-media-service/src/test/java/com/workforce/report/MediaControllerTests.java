package com.workforce.report;

import com.workforce.report.dto.MediaReportResponse;
import com.workforce.report.model.MediaReport;
import com.workforce.report.repository.MediaReportRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
        "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"
})
@AutoConfigureMockMvc
class MediaControllerTests {

    private static final String JWT_SECRET =
            "workforce-super-secret-jwt-key-change-in-production-min-256-bits";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaReportRepository repository;

    @Test
    void uploadValidJpegReturns201() throws Exception {
        MediaReport saved = savedReport("rapport.jpg", "image/jpeg", 4L, null);
        when(repository.save(any())).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile(
                "file", "rapport.jpg", "image/jpeg", fakeJpegBytes());

        mockMvc.perform(multipart("/api/media/upload")
                        .file(file)
                        .param("employeeId", "4")
                        .param("note", "Testfoto")
                        .header("Authorization", "Bearer " + token("emp.meier", "EMPLOYEE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("rapport.jpg"))
                .andExpect(jsonPath("$.employeeId").value(4));
    }

    @Test
    void uploadWithoutTokenReturns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "rapport.jpg", "image/jpeg", fakeJpegBytes());

        mockMvc.perform(multipart("/api/media/upload")
                        .file(file)
                        .param("employeeId", "4"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadNonImageReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "document.txt", "text/plain", "kein bild".getBytes());

        mockMvc.perform(multipart("/api/media/upload")
                        .file(file)
                        .param("employeeId", "4")
                        .header("Authorization", "Bearer " + token("emp.meier", "EMPLOYEE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nur Bilddateien sind erlaubt"));
    }

    @Test
    void uploadPngReturns201() throws Exception {
        MediaReport saved = savedReport("foto.png", "image/png", 4L, null);
        when(repository.save(any())).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.png", "image/png", fakeJpegBytes());

        mockMvc.perform(multipart("/api/media/upload")
                        .file(file)
                        .param("employeeId", "4")
                        .header("Authorization", "Bearer " + token("emp.meier", "EMPLOYEE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeId").value(4));
    }

    @Test
    void getImageByUnknownIdReturns404() throws Exception {
        when(repository.findById("unknown-id-xyz")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/media/unknown-id-xyz")
                        .header("Authorization", "Bearer " + token("emp.meier", "EMPLOYEE")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listByEmployeeReturnsUploadedReports() throws Exception {
        MediaReport report = savedReport("foto1.jpg", "image/jpeg", 7L, null);
        when(repository.findByEmployeeIdOrderByUploadedAtDesc(7L)).thenReturn(List.of(report));

        mockMvc.perform(get("/api/media/employee/7")
                        .header("Authorization", "Bearer " + token("hr.mueller", "HR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].employeeId").value(7));
    }

    @Test
    void uploadWithOrderIdStoredCorrectly() throws Exception {
        MediaReport saved = savedReport("auftrag.jpg", "image/jpeg", 4L, 99L);
        when(repository.save(any())).thenReturn(saved);

        MockMultipartFile file = new MockMultipartFile(
                "file", "auftrag.jpg", "image/jpeg", fakeJpegBytes());

        mockMvc.perform(multipart("/api/media/upload")
                        .file(file)
                        .param("employeeId", "4")
                        .param("orderId", "99")
                        .header("Authorization", "Bearer " + token("emp.meier", "EMPLOYEE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(99));
    }

    private MediaReport savedReport(String filename, String contentType, Long employeeId, Long orderId) {
        MediaReport r = new MediaReport();
        r.setId("test-id-" + filename.hashCode());
        r.setFilename(filename);
        r.setContentType(contentType);
        r.setEmployeeId(employeeId);
        r.setOrderId(orderId);
        r.setRapportId("rapport-uuid");
        r.setFileSize(4);
        r.setStoragePath("mongodb://test/" + filename);
        r.setUploadedAt(Instant.now());
        r.setMetadata(Map.of());
        r.setData(fakeJpegBytes());
        return r;
    }

    // Minimale gültige JPEG-Bytes (SOI + EOI marker)
    private byte[] fakeJpegBytes() {
        return new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9};
    }

    private String token(String username, String role) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86_400_000L))
                .signWith(key)
                .compact();
    }
}
