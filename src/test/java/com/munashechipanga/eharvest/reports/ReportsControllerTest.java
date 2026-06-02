package com.munashechipanga.eharvest.reports;

import com.munashechipanga.eharvest.reports.dto.ReportDescriptor;
import com.munashechipanga.eharvest.reports.exceptions.ReportGenerationException;
import com.munashechipanga.eharvest.reports.exceptions.ReportNotAllowedException;
import com.munashechipanga.eharvest.security.JwtFilter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ReportApiExceptionHandler.class, ReportsControllerTest.MockBeanConfiguration.class})
class ReportsControllerTest {

    @Autowired
    private ReportService reportService;

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockBeanConfiguration {
        @Bean
        ReportService reportService() {
            return mock(ReportService.class);
        }

        @Bean
        JwtFilter jwtFilter() {
            return mock(JwtFilter.class);
        }
    }

    @Test
    void availableReportsReturnsOnlyAllowedReportsForUser() throws Exception {
        when(reportService.availableReportsForUser(ArgumentMatchers.any()))
                .thenReturn(List.of(
                        new ReportDescriptor(
                                "sales_summary",
                                "Sales Summary",
                                "Revenue and orders",
                                List.of("ROLE_ADMIN", "ROLE_BUYER"),
                                List.of("from", "to")
                        )
                ));

        mockMvc.perform(get("/api/reports/available").with(user("buyer").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reportName").value("sales_summary"))
                .andExpect(jsonPath("$[0].label").value("Sales Summary"))
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)));
    }

    @Test
    void generateReturnsPdfForAllowedRole() throws Exception {
        byte[] pdf = "%PDF-1.4\nreport".getBytes();
        when(reportService.generatePdf(ArgumentMatchers.eq("sales_summary"), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(pdf);

        mockMvc.perform(get("/api/reports/generate/sales_summary")
                        .param("from", "2026-01-01")
                        .param("to", "2026-05-26")
                        .with(user("buyer").roles("BUYER")))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("sales_summary_2026-01-01_2026-05-26.pdf")))
                .andExpect(content().bytes(pdf));
    }

    @Test
    void generateReturnsForbiddenForDisallowedRole() throws Exception {
        when(reportService.generatePdf(ArgumentMatchers.eq("sales_summary"), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new ReportNotAllowedException("You are not allowed to generate report 'sales_summary'."));

        mockMvc.perform(get("/api/reports/generate/sales_summary")
                        .param("from", "2026-01-01")
                        .param("to", "2026-05-26")
                        .with(user("farmer").roles("FARMER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not allowed to generate report 'sales_summary'."));
    }

    @Test
    void generateReturnsPdfCompatibleErrorWhenClientAcceptsPdfOnly() throws Exception {
        when(reportService.generatePdf(ArgumentMatchers.eq("sales_summary"), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenThrow(new ReportGenerationException("Failed to load report stylesheet.", new RuntimeException("missing css")));

        mockMvc.perform(get("/api/reports/generate/sales_summary")
                        .param("from", "2026-01-01")
                        .param("to", "2026-05-26")
                        .accept(MediaType.APPLICATION_PDF)
                        .with(user("buyer").roles("BUYER")))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(content().bytes(new byte[0]));
    }
}
