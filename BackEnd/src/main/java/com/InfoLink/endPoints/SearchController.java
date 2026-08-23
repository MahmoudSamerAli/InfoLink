package com.InfoLink.endPoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.InfoLink.utils.PaginationUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;

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
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
            .getContext().getAuthentication().getPrincipal();
        Groups group = userDetails.getUser().getGroup();
        GroupsCollections gc = groupsCollectionsService.getCollectionForGroup(collection, group);

        // Remove "collection" param so only search fields remain
        params.remove("collection");

        List<Criteria> criteriaList = new ArrayList<>();
        params.forEach((field, keyword) -> {
            criteriaList.add(Criteria.where(field).regex(keyword, "i"));
        });

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
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
    

}