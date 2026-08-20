package com.InfoLink.service;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.InfoLink.model.Groups;
import com.InfoLink.model.GroupsCollections;
import com.InfoLink.repository.GroupRepository;
import com.InfoLink.repository.GroupsCollectionsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroupsCollectionsService {

    private final GroupsCollectionsRepository repo;
    private final GroupRepository groupsRepository;
    private final MongoTemplate mongoTemplate;
    private static final int MAX_RECORDS = 50_000;


    public GroupsCollectionsService(GroupsCollectionsRepository repo,
                                     GroupRepository groupsRepository,
                                     MongoTemplate mongoTemplate) {
        this.repo = repo;
        this.groupsRepository = groupsRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<GroupsCollections> getCollectionsForGroup(Long groupId) {
        return repo.findByGroup_GroupID(groupId);
    }

    public GroupsCollections getCollectionForGroup(String collectionName, Groups group) {
        return repo.findByCollectionNameAndGroup(collectionName, group)
                   .orElseThrow(() -> new RuntimeException("Collection not accessible for this group"));
    }

    public List<GroupsCollections> getGroupsForCollection(String collectionName) {
        return repo.findByCollectionName(collectionName);
    }

    /** Creates the Mongo collection, optionally granting one group access immediately. Rolls back the Mongo collection if the SQL insert fails. */
    @Transactional
    public void createCollection(String collectionName, Long initialGroupId) {
        if (mongoTemplate.collectionExists(collectionName)) {
            throw new IllegalStateException("Collection already exists");
        }
        mongoTemplate.createCollection(collectionName);

        if (initialGroupId != null) {
            try {
                grantAccess(collectionName, initialGroupId);
            } catch (RuntimeException e) {
                // Compensate: Mongo isn't part of the JPA transaction, so undo it manually.
                mongoTemplate.dropCollection(collectionName);
                throw e;
            }
        }
    }

    /** Drops the Mongo collection and removes all group-access rows for it. */
    @Transactional
    public void deleteCollection(String collectionName) {
        if (!mongoTemplate.collectionExists(collectionName)) {
            throw new IllegalStateException("Collection does not exist");
        }
        repo.findByCollectionName(collectionName).forEach(repo::delete);
        mongoTemplate.dropCollection(collectionName);
    }

    public void grantAccess(String collectionName, Long groupId) {
        if (repo.existsByCollectionNameAndGroup_GroupID(collectionName, groupId)) {
            return; // already granted, no-op
        }
        Groups group = groupsRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

        GroupsCollections gc = new GroupsCollections();
        gc.setCollectionName(collectionName);
        gc.setGroup(group);
        repo.save(gc);
    }

    public void revokeAccess(String collectionName, Long groupId) {
        repo.deleteByCollectionNameAndGroup_GroupID(collectionName, groupId);
    }

    public int uploadData(String collectionName, MultipartFile file) throws IOException {
    if (!mongoTemplate.collectionExists(collectionName)) {
        throw new IllegalStateException("Collection does not exist: " + collectionName);
    }

    if (file == null || file.isEmpty()) {
        throw new IllegalArgumentException("Uploaded file is empty");
    }

    String filename = file.getOriginalFilename();
    List<Document> documents;

    if (filename != null && filename.toLowerCase().endsWith(".json")) {
        documents = parseJson(file);
    } else if (filename != null && filename.toLowerCase().endsWith(".csv")) {
        documents = parseCsv(file);
    } else {
        throw new IllegalArgumentException("Unsupported file type. Upload a .csv or .json file.");
    }

    if (documents.isEmpty()) {
        throw new IllegalArgumentException("No records found in uploaded file");
    }
    if (documents.size() > MAX_RECORDS) {
        throw new IllegalArgumentException("File exceeds maximum of " + MAX_RECORDS + " records per upload");
    }
    documents.forEach(this::validateFieldNames);

    mongoTemplate.insert(documents, collectionName);
    return documents.size();
}

private void validateFieldNames(Document doc) {
    for (String key : doc.keySet()) {
        if (key.startsWith("$") || key.contains(".")) {
            throw new IllegalArgumentException(
                "Invalid field name '" + key + "': cannot start with '$' or contain '.'");
        }
    }
}

private List<Document> parseJson(MultipartFile file) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    List<Map<String, Object>> records = mapper.readValue(
        file.getInputStream(), new TypeReference<List<Map<String, Object>>>() {});

    List<Document> docs = new ArrayList<>();
    for (Map<String, Object> record : records) {
        Document doc = new Document();
        for (Map.Entry<String, Object> entry : record.entrySet()) {
            doc.put(entry.getKey(), stringifyValue(entry.getKey(), entry.getValue()));
        }
        docs.add(doc);
    }
    return docs;
}

private String stringifyValue(String fieldName, Object value) {
    if (value == null) {
        return "";
    }
    if (value instanceof Map || value instanceof List) {
        throw new IllegalArgumentException(
            "Field '" + fieldName + "' contains a nested object or array, which isn't supported. " +
            "Upload flat key-value records only (every field must be a plain value).");
    }
    return String.valueOf(value); // numbers, booleans -> their string form
}

private List<Document> parseCsv(MultipartFile file) throws IOException {
    List<Document> docs = new ArrayList<>();
    try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
         CSVParser parser = CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setSkipHeaderRecord(true)
                 .setTrim(true)
                 .build()
                 .parse(reader)) {

        for (CSVRecord record : parser) {
            Document doc = new Document();
            for (String header : parser.getHeaderNames()) {
                doc.put(header, record.get(header));
            }
            docs.add(doc);
        }
    }
    return docs;
}
}
