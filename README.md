# eHarvest Reports API

## Report Endpoints

- `GET /api/reports/available`
- `GET /api/reports/generate/{reportName}?from=YYYY-MM-DD&to=YYYY-MM-DD`

Both endpoints require `Authorization: Bearer <token>`. The backend reuses the existing JWT filter and resolves the authenticated user's role from Spring Security authorities derived from the local `UserDetailsService`.

## PDF Generation

- Templates live under `src/main/resources/templates/reports/`
- CSS lives at `src/main/resources/templates/reports/report.css`
- PDFs are rendered with Thymeleaf plus `openhtmltopdf-pdfbox`

## Notes

- Current JWT generation stores `role` and `userId` claims. Report authorization uses the existing security flow, which loads authorities from the local user record. If your external issuer later sends `roles` or `authorities` claims directly, the claim-to-authority mapping should be updated in `src/main/java/com/munashechipanga/eharvest/security/JwtFilter.java`.
- Heavy reports are protected with a configurable timeout via `eharvest.reports.generation-timeout-seconds`.
- For larger datasets in production, prefer scheduled pre-aggregation, materialized views, or streaming directly to the HTTP response output stream.

## Example cURL

```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/reports/available
curl -H "Authorization: Bearer <token>" -o report.pdf "http://localhost:8080/api/reports/generate/sales_summary?from=2026-01-01&to=2026-05-26"
```
