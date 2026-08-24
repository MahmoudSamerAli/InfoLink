package com.InfoLink.endPoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;

import com.InfoLink.model.Groups;
import com.InfoLink.model.GroupsCollections;
import com.InfoLink.model.Log;
import com.InfoLink.security.CustomUserDetails;
import com.InfoLink.service.GroupsCollectionsService;
import com.InfoLink.service.LogService;
import com.InfoLink.dto.PagedResponse;
import com.InfoLink.dto.DeepSearchRequest;
import com.InfoLink.dto.DeepSearchResult;
import com.InfoLink.utils.PaginationUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/search")
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final GroupsCollectionsService groupsCollectionsService;
    private final LogService logService;
    private final MongoTemplate mongoTemplate;

    public SearchController(GroupsCollectionsService groupsCollectionsService,
                            LogService logService,
                            MongoTemplate mongoTemplate) {
        this.groupsCollectionsService = groupsCollectionsService;
        this.logService = logService;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping
    public PagedResponse<Document> search(@RequestParam String collection,
                                      @RequestParam Map<String, String> params,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size,
                                      HttpServletRequest request) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
            .getContext().getAuthentication().getPrincipal();
        Groups group = userDetails.getUser().getGroup();
        GroupsCollections gc = groupsCollectionsService.getCollectionForGroup(collection, group);

        // Remove "collection" param so only search fields remain
        params.remove("collection");
        params.remove("page");
        params.remove("size");
        java.util.Set<String> validFields = new java.util.HashSet<>(groupsCollectionsService.getFieldsForCollection(collection));
        if (!validFields.containsAll(params.keySet())) {
            throw new IllegalArgumentException("Unsupported search field supplied");
        }

        List<Criteria> criteriaList = new ArrayList<>();
        params.forEach((field, keyword) -> {
            if (field == null || field.isBlank() || keyword == null || keyword.isBlank()) {
                throw new IllegalArgumentException("Search fields and values cannot be empty");
            }
            criteriaList.add(Criteria.where(field).regex(java.util.regex.Pattern.quote(keyword), "i"));
        });

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria = new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
        }

        Query query = new Query(criteria)
            .skip(page * size)
            .limit(size);

        List<Document> results = mongoTemplate.find(query, Document.class, gc.getCollectionName());
        long total = mongoTemplate.count(new Query(criteria), gc.getCollectionName());

        Log log = new Log();
        log.setUser(userDetails.getUser());
        log.setCollection(gc.getCollectionName());
        log.setSearchKeyword(params.toString());
        log.setSearchDate(LocalDateTime.now());
        log.setIpAddress(request.getRemoteAddr());
        log.setStatus(!results.isEmpty());
        logService.saveLog(log);

        return PaginationUtil.buildPagedResponse(results, page, size, total);
    }

    @GetMapping("/deep/fields")
    public List<String> getDeepSearchFields() {
        return groupsCollectionsService.getCommonFields();
    }

    @GetMapping("/fields")
    public List<String> getSearchFields() {
        return groupsCollectionsService.getCommonFields();
    }

    @GetMapping("/fields/{collectionName}")
    public List<String> getFieldsForCollection(@PathVariable String collectionName) {
        return groupsCollectionsService.getFieldsForCollection(collectionName);
    }

    @PostMapping("/deep")
    public PagedResponse<DeepSearchResult> deepSearch(
            @Valid @RequestBody DeepSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        List<String> collectionNames = mongoTemplate.getCollectionNames().stream()
            .sorted()
            .toList();
        List<String> commonFields = groupsCollectionsService.getCommonFields();
        java.util.Set<String> invalidFields = new java.util.HashSet<>(request.getSearchKeys().keySet());
        invalidFields.removeAll(commonFields);
        if (!invalidFields.isEmpty()) {
            throw new IllegalArgumentException("Search keys are not common to all selected collections: " + invalidFields);
        }

        List<Criteria> criteriaList = new ArrayList<>();
        request.getSearchKeys().forEach((field, keyword) -> {
            if (field == null || field.isBlank() || keyword == null || keyword.isBlank()) {
                throw new IllegalArgumentException("Search fields and values cannot be empty");
            }
            criteriaList.add(Criteria.where(field).regex(java.util.regex.Pattern.quote(keyword), "i"));
        });
        Criteria criteria = new Criteria().orOperator(criteriaList.toArray(new Criteria[0]));
        long requestedOffset = (long) page * size;
        long skipped = 0;
        long total = 0;
        List<DeepSearchResult> results = new ArrayList<>();

        for (String collectionName : collectionNames) {
            long collectionTotal = mongoTemplate.count(new Query(criteria), collectionName);
            total += collectionTotal;
            if (skipped + collectionTotal <= requestedOffset) {
                skipped += collectionTotal;
                continue;
            }
            long collectionSkip = Math.max(0, requestedOffset - skipped);
            int remaining = size - results.size();
            List<Document> documents = mongoTemplate.find(
                    new Query(criteria).skip(collectionSkip).limit(remaining), Document.class, collectionName);
            documents.forEach(document -> results.add(new DeepSearchResult(collectionName, document)));
            skipped += collectionTotal;
            if (results.size() == size) {
                break;
            }
        }

        Log log = new Log();
        log.setUser(userDetails.getUser());
        log.setCollection(String.join(",", collectionNames));
        log.setSearchKeyword(request.getSearchKeys().toString());
        log.setSearchDate(LocalDateTime.now());
        log.setIpAddress(httpRequest.getRemoteAddr());
        log.setStatus(!results.isEmpty());
        logService.saveLog(log);

        return PaginationUtil.buildPagedResponse(results, page, size, total);
    }
    

}