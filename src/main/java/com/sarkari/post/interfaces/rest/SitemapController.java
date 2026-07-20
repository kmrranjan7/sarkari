package com.sarkari.post.interfaces.rest;

import com.sarkari.post.application.dto.response.SitemapUrlResponse;
import com.sarkari.post.application.service.PublicPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/site")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sitemap", description = "Sitemap APIs")
public class SitemapController {

    private static final int SITEMAP_CHUNK_SIZE = 1000;
    private static final DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final PublicPostService publicPostService;

    @GetMapping(value = "/post-sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Primary post sitemap", description = "Fetch first post sitemap file with up to 1000 URLs")
    public ResponseEntity<String> getPrimaryPostSitemap() {
        log.info("Primary post sitemap request received");
        return buildPostSitemapByPage(1);
    }

    @GetMapping(value = "/post-sitemap{suffix:\\d+}.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Chunked post sitemap", description = "Fetch chunked post sitemap files where each file contains up to 1000 URLs")
    public ResponseEntity<String> getPostSitemapBySuffix(@PathVariable("suffix") int suffix) {
        log.info("Chunked post sitemap request received suffix={}", suffix);
        if (suffix < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Naming rule: post-sitemap.xml is chunk 1, post-sitemap1.xml is chunk 2, and so on.
        int page = suffix + 1;
        return buildPostSitemapByPage(page);
    }

    @GetMapping(value = "/post-sitemap-page{page:\\d+}.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Legacy chunked post sitemap", description = "Backward-compatible alias where page number directly maps to chunk number")
    public ResponseEntity<String> getPostSitemapLegacy(@PathVariable("page") int page) {
        log.info("Legacy post sitemap request received page={}", page);
        return buildPostSitemapByPage(page);
    }

    private ResponseEntity<String> buildPostSitemapByPage(int page) {
        log.info("Build post sitemap page request page={}", page);
        if (page < 1) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        List<SitemapUrlResponse> allUrls = publicPostService.getSitemapUrls();
        int fromIndex = (page - 1) * SITEMAP_CHUNK_SIZE;

        if (fromIndex >= allUrls.size()) {
            log.info("Post sitemap page out of bounds page={} totalUrls={}", page, allUrls.size());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        int toIndex = Math.min(fromIndex + SITEMAP_CHUNK_SIZE, allUrls.size());
        List<SitemapUrlResponse> chunk = allUrls.subList(fromIndex, toIndex);
        log.info("Post sitemap page prepared page={} fromIndex={} toIndexExclusive={} chunkSize={}",
            page, fromIndex, toIndex, chunk.size());

        String xml = buildUrlSetXml(chunk);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    @GetMapping(value = "/sitemap-index.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Sitemap index", description = "Fetch sitemap index linking all post sitemap chunk files")
    public ResponseEntity<String> getSitemapIndex() {
        log.info("Sitemap index request received");
        List<SitemapUrlResponse> allUrls = publicPostService.getSitemapUrls();
        int totalPages = Math.max(1, (int) Math.ceil((double) allUrls.size() / SITEMAP_CHUNK_SIZE));
        List<SitemapEntry> sitemapUrls = new ArrayList<>(totalPages + 2);
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String lastModified = LocalDateTime.now(ZoneOffset.UTC).atOffset(ZoneOffset.UTC).format(ISO_OFFSET_FORMATTER);

        for (int i = 1; i <= totalPages; i++) {
            if (i == 1) {
                sitemapUrls.add(new SitemapEntry(baseUrl + "/api/site/post-sitemap.xml", lastModified));
            } else {
                sitemapUrls.add(new SitemapEntry(baseUrl + "/api/site/post-sitemap" + (i - 1) + ".xml", lastModified));
            }
        }

        sitemapUrls.add(new SitemapEntry(baseUrl + "/page-sitemap.xml", lastModified));
        sitemapUrls.add(new SitemapEntry(baseUrl + "/category-sitemap.xml", lastModified));
        log.info("Sitemap index prepared totalUrls={} totalPages={} totalEntries={}",
            allUrls.size(), totalPages, sitemapUrls.size());

        String xml = buildSitemapIndexXml(sitemapUrls);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    private static String buildUrlSetXml(List<SitemapUrlResponse> urls) {
        StringBuilder sb = new StringBuilder(4096);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" ")
            .append("xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\">");

        for (SitemapUrlResponse url : urls) {
            sb.append("<url>")
                    .append("<loc>").append(escapeXml(url.loc())).append("</loc>");

            if (url.lastModified() != null && !url.lastModified().isBlank()) {
                sb.append("<lastmod>").append(escapeXml(url.lastModified())).append("</lastmod>");
            }

            if (url.imageLoc() != null && !url.imageLoc().isBlank()) {
                sb.append("<image:image>")
                        .append("<image:loc>").append(escapeXml(url.imageLoc())).append("</image:loc>")
                        .append("</image:image>");
            }

            sb.append("</url>");
        }

        sb.append("</urlset>");
        return sb.toString();
    }

    private static String buildSitemapIndexXml(List<SitemapEntry> sitemapEntries) {
        StringBuilder sb = new StringBuilder(2048);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");

        for (SitemapEntry sitemapEntry : sitemapEntries) {
            sb.append("<sitemap>")
                    .append("<loc>").append(escapeXml(sitemapEntry.loc())).append("</loc>")
                    .append("<lastmod>").append(escapeXml(sitemapEntry.lastMod())).append("</lastmod>")
                    .append("</sitemap>");
        }

        sb.append("</sitemapindex>");
        return sb.toString();
    }

    private static String escapeXml(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private record SitemapEntry(String loc, String lastMod) {
    }
}
