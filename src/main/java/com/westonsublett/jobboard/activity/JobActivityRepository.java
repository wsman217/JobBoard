package com.westonsublett.jobboard.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface JobActivityRepository
        extends JpaRepository<JobActivity, UUID>, JpaSpecificationExecutor<JobActivity> {
}
