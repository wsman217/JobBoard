package com.westonsublett.jobboard.activity;

import com.westonsublett.jobboard.user.User;
import com.westonsublett.jobboard.user.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JobActivityService {

    private final JobActivityRepository jobActivityRepository;
    private final UserRepository userRepository;

    public JobActivityService(JobActivityRepository jobActivityRepository,
                              UserRepository userRepository) {
        this.jobActivityRepository = jobActivityRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<JobActivity> list(UUID userId, String search, String searchType, String jobType, String status) {
        Specification<JobActivity> spec = (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("companyName")), pattern),
                    cb.like(cb.lower(root.get("jobType")), pattern),
                    cb.like(cb.lower(root.get("searchType")), pattern),
                    cb.like(cb.lower(root.get("status")), pattern)
            ));
        }
        if (searchType != null && !searchType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("searchType"), searchType));
        }
        if (jobType != null && !jobType.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("jobType"), jobType));
        }
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Sort sort = Sort.by(Sort.Order.desc("activityDate"), Sort.Order.desc("createdAt"));
        return jobActivityRepository.findAll(spec, sort);
    }

    @Transactional
    public JobActivity create(UUID userId, Instant activityDate, String searchType, String jobType,
                              String companyName, String status) {
        if (activityDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activityDate is required");
        }
        requireText(searchType, "searchType");
        requireText(jobType, "jobType");
        requireText(companyName, "companyName");
        requireText(status, "status");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        JobActivity activity = new JobActivity();
        activity.setUser(user);
        activity.setActivityDate(activityDate);
        activity.setSearchType(searchType.trim());
        activity.setJobType(jobType.trim());
        activity.setCompanyName(companyName.trim());
        activity.setStatus(status.trim());

        return jobActivityRepository.save(activity);
    }

    @Transactional
    public JobActivity update(UUID userId, UUID id, Instant activityDate, String searchType, String jobType,
                              String companyName, String status) {
        if (activityDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activityDate is required");
        }
        requireText(searchType, "searchType");
        requireText(jobType, "jobType");
        requireText(companyName, "companyName");
        requireText(status, "status");

        JobActivity activity = findOwnedActivity(userId, id);
        activity.setActivityDate(activityDate);
        activity.setSearchType(searchType.trim());
        activity.setJobType(jobType.trim());
        activity.setCompanyName(companyName.trim());
        activity.setStatus(status.trim());

        return jobActivityRepository.saveAndFlush(activity);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        JobActivity activity = findOwnedActivity(userId, id);
        jobActivityRepository.delete(activity);
    }

    private JobActivity findOwnedActivity(UUID userId, UUID id) {
        return jobActivityRepository.findById(id)
                .filter(activity -> activity.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job activity not found"));
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
    }
}
