package com.munashechipanga.eharvest.reports;

import com.munashechipanga.eharvest.reports.exceptions.ReportGenerationException;
import com.munashechipanga.eharvest.reports.exceptions.ReportNotAllowedException;
import com.munashechipanga.eharvest.reports.exceptions.ReportNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = ReportsController.class)
public class ReportApiExceptionHandler {

    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ReportNotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ReportNotAllowedException.class)
    public ResponseEntity<?> handleForbidden(ReportNotAllowedException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ReportGenerationException.class)
    public ResponseEntity<?> handleGenerationError(ReportGenerationException ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<?> error(HttpStatus status, String message, HttpServletRequest request) {
        if (acceptsPdf(request)) {
            return ResponseEntity.status(status)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(new byte[0]);
        }

        return ResponseEntity.status(status).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        ));
    }

    private boolean acceptsPdf(HttpServletRequest request) {
        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (acceptHeader == null || acceptHeader.isBlank()) {
            return false;
        }

        List<MediaType> acceptedTypes = MediaType.parseMediaTypes(acceptHeader);
        MediaType.sortBySpecificityAndQuality(acceptedTypes);
        return acceptedTypes.stream().anyMatch(MediaType.APPLICATION_PDF::isCompatibleWith);
    }
}
