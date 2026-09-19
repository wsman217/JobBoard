package com.westonsublett.jobboard.activity;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-activities")
public class JobActivityController {

    private final JobActivityService jobActivityService;

    public JobActivityController(JobActivityService jobActivityService) {
        this.jobActivityService = jobActivityService;
    }

    @GetMapping
    public List<JobActivityResponse> list(@RequestParam(required = false) String search,
                                          @RequestParam(required = false) String searchType,
                                          @RequestParam(required = false) String jobType,
                                          @RequestParam(required = false) String status) {
        UUID userId = currentUserId();
        return jobActivityService.list(userId, search, searchType, jobType, status)
                .stream()
                .map(JobActivityResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JobActivityResponse create(@RequestBody CreateJobActivityRequest request) {
        UUID userId = currentUserId();
        JobActivity activity = jobActivityService.create(
                userId,
                request.activityDate(),
                request.searchType(),
                request.jobType(),
                request.companyName(),
                request.status());
        return JobActivityResponse.from(activity);
    }

    @PutMapping("/{id}")
    public JobActivityResponse update(@PathVariable UUID id, @RequestBody CreateJobActivityRequest request) {
        UUID userId = currentUserId();
        JobActivity activity = jobActivityService.update(
                userId,
                id,
                request.activityDate(),
                request.searchType(),
                request.jobType(),
                request.companyName(),
                request.status());
        return JobActivityResponse.from(activity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        jobActivityService.delete(currentUserId(), id);
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)
                || jwt.getSubject() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return UUID.fromString(jwt.getSubject());
    }

    public record JobActivityResponse(UUID id,
                                      Instant activityDate,
                                      String searchType,
                                      String jobType,
                                      String companyName,
                                      String status) {

        static JobActivityResponse from(JobActivity activity) {
            return new JobActivityResponse(
                    activity.getId(),
                    activity.getActivityDate(),
                    activity.getSearchType(),
                    activity.getJobType(),
                    activity.getCompanyName(),
                    activity.getStatus());
        }
    }

    public record CreateJobActivityRequest(Instant activityDate,
                                           String searchType,
                                           String jobType,
                                           String companyName,
                                           String status) {
    }
}
