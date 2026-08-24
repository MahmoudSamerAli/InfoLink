package com.InfoLink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.InfoLink.model.Groups;


public interface GroupRepository extends JpaRepository<Groups, Long> {
	    @Query("SELECT g FROM Groups g WHERE " +
		    "(:keyword IS NULL OR LOWER(g.groupName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		    "OR LOWER(g.groupDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
		    "AND (:active IS NULL OR g.isActive = :active)")
	    Page<Groups> search(@Param("keyword") String keyword, @Param("active") Boolean active,
				   Pageable pageable);
}
