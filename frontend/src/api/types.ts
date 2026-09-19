export interface JobActivity {
    id: string;
    activityDate: string;
    searchType: string;
    jobType: string;
    companyName: string;
    status: string;
}

export interface JobActivityFilters {
    search?: string;
    searchType?: string;
    jobType?: string;
    status?: string;
}

export type NewJobActivity = Omit<JobActivity, "id">;

export interface JobActivitiesApi {
    list(filters?: JobActivityFilters): Promise<JobActivity[]>;
    create(activity: NewJobActivity): Promise<JobActivity>;
    update(id: string, activity: NewJobActivity): Promise<JobActivity>;
    delete(id: string): Promise<void>;
}