import type {JobActivitiesApi, JobActivity, JobActivityFilters, NewJobActivity} from "./types";

const NETWORK_DELAY_MS = 250;

const delay = (milliseconds: number) =>
    new Promise<void>(resolve => setTimeout(resolve, milliseconds));

const seedActivities: JobActivity[] = [
    {
        id: "act-001",
        activityDate: "2026-09-14T00:00:00.000Z",
        searchType: "Applied for Job",
        jobType: "Full-time",
        companyName: "Acme Corporation",
        status: "Pending"
    },
    {
        id: "act-002",
        activityDate: "2026-09-11T00:00:00.000Z",
        searchType: "Submitted Resume",
        jobType: "Part-time",
        companyName: "Globex Industries",
        status: "In Progress"
    },
    {
        id: "act-003",
        activityDate: "2026-09-08T00:00:00.000Z",
        searchType: "Job Fair",
        jobType: "Contract",
        companyName: "Initech",
        status: "Accepted"
    },
    {
        id: "act-004",
        activityDate: "2026-09-02T00:00:00.000Z",
        searchType: "Interview",
        jobType: "Internship",
        companyName: "Umbrella Corp",
        status: "Rejected"
    },
    {
        id: "act-005",
        activityDate: "2026-08-27T00:00:00.000Z",
        searchType: "Applied for Job",
        jobType: "Full-time",
        companyName: "Stark Industries",
        status: "Pending"
    }
];

const matchesFilters = (activity: JobActivity, filters: JobActivityFilters): boolean => {
    const query = filters.search?.trim().toLowerCase();
    if (query) {
        const haystack = [
            activity.companyName,
            activity.jobType,
            activity.searchType,
            activity.status
        ].join(" ").toLowerCase();
        if (!haystack.includes(query)) {
            return false;
        }
    }
    if (filters.searchType && activity.searchType !== filters.searchType) {
        return false;
    }
    if (filters.jobType && activity.jobType !== filters.jobType) {
        return false;
    }
    return !(filters.status && activity.status !== filters.status);

};

export class MockApi implements JobActivitiesApi {
    private activities: JobActivity[] = [...seedActivities];

    async list(filters: JobActivityFilters = {}): Promise<JobActivity[]> {
        await delay(NETWORK_DELAY_MS);
        return this.activities.filter(activity => matchesFilters(activity, filters));
    }

    async create(activity: NewJobActivity): Promise<JobActivity> {
        await delay(NETWORK_DELAY_MS);
        const created: JobActivity = {
            id: `act-${this.activities.length + 1}`,
            ...activity
        };
        this.activities = [created, ...this.activities];
        return created;
    }

    async update(id: string, activity: NewJobActivity): Promise<JobActivity> {
        await delay(NETWORK_DELAY_MS);
        const index = this.activities.findIndex(existing => existing.id === id);
        if (index === -1) {
            throw new Error("Job activity not found");
        }
        const updated: JobActivity = {id, ...activity};
        this.activities = [
            ...this.activities.slice(0, index),
            updated,
            ...this.activities.slice(index + 1)
        ];
        return updated;
    }

    async delete(id: string): Promise<void> {
        await delay(NETWORK_DELAY_MS);
        this.activities = this.activities.filter(activity => activity.id !== id);
    }
}