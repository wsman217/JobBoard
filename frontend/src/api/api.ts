import type {JobActivitiesApi, JobActivity, JobActivityFilters, NewJobActivity} from "./types";

const API_URL = import.meta.env.VITE_API_URL;

const buildQuery = (filters: JobActivityFilters): string => {
    const params = new URLSearchParams();
    if (filters.search) params.set("search", filters.search);
    if (filters.searchType) params.set("searchType", filters.searchType);
    if (filters.jobType) params.set("jobType", filters.jobType);
    if (filters.status) params.set("status", filters.status);
    return params.toString();
};

export const api: JobActivitiesApi = {
    async list(filters = {}) {
        const query = buildQuery(filters);
        const response = await fetch(`${API_URL}/job-activities${query ? `?${query}` : ""}`, {
            credentials: "include"
        });
        if (!response.ok) {
            throw new Error(`Failed to load job activities (${response.status})`);
        }
        return await response.json() as Promise<JobActivity[]>;
    },

    async create(activity: NewJobActivity) {
        const response = await fetch(`${API_URL}/job-activities`, {
            method: "POST",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(activity)
        });
        if (!response.ok) {
            throw new Error(`Failed to create job activity (${response.status})`);
        }
        return await response.json() as Promise<JobActivity>;
    },

    async update(id: string, activity: NewJobActivity) {
        const response = await fetch(`${API_URL}/job-activities/${id}`, {
            method: "PUT",
            credentials: "include",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(activity)
        });
        if (!response.ok) {
            throw new Error(`Failed to update job activity (${response.status})`);
        }
        return await response.json() as Promise<JobActivity>;
    },

    async delete(id: string) {
        const response = await fetch(`${API_URL}/job-activities/${id}`, {
            method: "DELETE",
            credentials: "include"
        });
        if (!response.ok) {
            throw new Error(`Failed to delete job activity (${response.status})`);
        }
    }
};