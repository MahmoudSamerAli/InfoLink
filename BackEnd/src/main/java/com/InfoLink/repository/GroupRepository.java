package com.InfoLink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.InfoLink.model.Groups;


public interface GroupRepository extends JpaRepository<Groups, Long> {}
