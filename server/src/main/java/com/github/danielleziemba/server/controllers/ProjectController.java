package com.github.danielleziemba.server.controllers;

import com.github.danielleziemba.server.exception.ResourceNotFoundException;
import com.github.danielleziemba.server.models.Project;
import com.github.danielleziemba.server.payload.response.ProjectResponse;
import com.github.danielleziemba.server.repository.ProjectRepository;
import com.github.danielleziemba.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

//@CrossOrigin
@RestController
@RequestMapping("/api")
public class ProjectController {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponse>> getAllProjects(@RequestParam(required = false) String title) {
//        List<Project> projects = new ArrayList<>();
        List<ProjectResponse> projects = new ArrayList<>();

        if (title == null)
            projectRepository.findAll().forEach(project -> projects.add(
                    new ProjectResponse(
                            project.getId(),
                            project.getTitle(),
                            project.getDescription(),
                            project.getProjectManager().getUsername())));
        else
            projectRepository.findByTitleContaining(title).forEach(project -> projects.add(
                    new ProjectResponse(
                            project.getId(),
                            project.getTitle(),
                            project.getDescription(),
                            project.getProjectManager().getUsername())));

        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));

        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ROLE_PROJMANAGER')")
    @PostMapping("/users/{id}/projects/new")
    public ResponseEntity<ProjectResponse> createProject(@PathVariable Long id, @RequestBody Project projectRequest) {
        Project project = userRepository.findById(id).map(user -> {
            projectRequest.setProjectManager(user);
            return projectRepository.save(projectRequest);
        }).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        return new ResponseEntity<>(new ProjectResponse(
                project.getId(),
                project.getTitle(),
                projectRequest.getDescription(),
                project.getProjectManager().getUsername()
        ), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('PROJMANAGER') or hasRole('ADMIN')")
    @PutMapping("/projects/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id, @RequestBody Project projectRequest) {
        Project project = projectRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException("Project not found with id " + id));

        project.setTitle(projectRequest.getTitle());
        project.setDescription(projectRequest.getDescription());
        project.setProjectManager(projectRequest.getProjectManager());

        return new ResponseEntity<>(new ProjectResponse(
                project.getId(),
                project.getTitle(),
                projectRequest.getDescription(),
                project.getProjectManager().getUsername()
        ), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('PROJMANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/projects/{id}")
    public ResponseEntity<HttpStatus> deleteProjectById(@PathVariable Long id) {
        projectRepository.deleteById(id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('PROJMANAGER') or hasRole('ADMIN')")
    @DeleteMapping("/users/{id}/projects")
    public ResponseEntity<HttpStatus> deleteByProjectManagerId(@PathVariable(value = "id") Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id = " + userId);
        }

        projectRepository.deleteByProjectManagerId(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/users/{id}/projects")
    public ResponseEntity<List<Project>> getProjectsByManagerId(@PathVariable Long id) {
        List<Project> projects = projectRepository.findByProjectManagerId(id);

        if (projects.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

}
