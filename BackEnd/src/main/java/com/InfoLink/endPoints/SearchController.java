package com.InfoLink.endPoints;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import org.bson.Document;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.context.SecurityContextHolder;

import com.InfoLink.model.GroupsCollections;
import com.InfoLink.model.Log;
import com.InfoLink.security.CustomUserDetails;
import com.InfoLink.service.GroupsCollectionsService;
import com.InfoLink.service.LogService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/search")
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
    public List<Document> search(@RequestParam String collection,
                                 @RequestParam String field,
                                 @RequestParam String keyword,
                                 HttpServletRequest request) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Long groupId = userDetails.getGroupId();
        GroupsCollections gc = groupsCollectionsService.getCollectionForGroup(collection, groupId);
        Query query = new Query(Criteria.where(field).regex(keyword, "i"));
        List<Document> results = mongoTemplate.find(query, Document.class, collection);
        Log log = new Log();
        log.setUser(userDetails.getUser());
        log.setCollection(gc.getCollection());
        log.setSearchKeyword(keyword);
        log.setSearchDate(LocalDateTime.now());
        log.setIpAddress(request.getRemoteAddr());
        log.setStatus(!results.isEmpty());
        logService.saveLog(log);
        return results;
    }
}