package com.InfoLink.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.InfoLink.model.SearchMapping;

public interface SearchMappingRepository extends JpaRepository<SearchMapping, Long> {
    List<SearchMapping> findByCommonTrue();
    List<SearchMapping> findByCollectionName(String collectionName);
}