package com.github.danielleziemba.server.repository;

import com.github.danielleziemba.server.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByTitleContaining(String title);

    List<Project> findByProjectManagerId(Long id);

//    List<Project> findByManagerUsername(String username);

    @Transactional
    void deleteByProjectManagerId(Long userId);

}
