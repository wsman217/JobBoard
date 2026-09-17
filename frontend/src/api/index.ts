import {MockApi} from "./mockApi.ts";
import {api} from "./api.ts";
import type {JobActivitiesApi} from "./types";

const useMockApi = import.meta.env.VITE_USE_MOCK_API === "true";

export const jobActivitiesApi: JobActivitiesApi =
    useMockApi ? new MockApi() : api;

export type {JobActivitiesApi, JobActivity, JobActivityFilters, NewJobActivity} from "./types";
export {MockApi} from "./mockApi.ts";
export {api} from "./api.ts";