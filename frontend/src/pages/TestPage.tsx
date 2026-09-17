import {useCallback, useEffect, useState} from "react";
import Button from "../ui-components/Button";
import CalendarSelector from "../ui-components/CalendarSelector";
import Input from "../ui-components/Input";
import Modal from "../ui-components/Modal";
import SelectableInput, {SelectableInputOption} from "../ui-components/SelectableInput";
import {
    jobActivitiesApi,
    type JobActivity
} from "../api";
import "./TestPage.css";

const SEARCH_TYPES = ["Applied for Job", "Submitted Resume", "Job Fair", "Interview"];
const JOB_TYPES = ["Full-time", "Part-time", "Contract", "Internship"];
const STATUSES = ["Pending", "In Progress", "Accepted", "Rejected"];

const createEmptyForm = () => ({
    activityDate: new Date(),
    searchType: "",
    jobType: "",
    companyName: "",
    status: ""
});

type FormState = ReturnType<typeof createEmptyForm>;

const createEmptyFilters = () => ({search: "", searchType: "", jobType: "", status: ""});

type FiltersState = ReturnType<typeof createEmptyFilters>;

const TestPage = () => {
    const [entries, setEntries] = useState<JobActivity[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [filters, setFilters] = useState<FiltersState>(createEmptyFilters);
    const [draft, setDraft] = useState<FiltersState>(createEmptyFilters);
    const [filterModalOpen, setFilterModalOpen] = useState(false);
    const [createOpen, setCreateOpen] = useState(false);
    const [form, setForm] = useState<FormState>(createEmptyForm);
    const [submitting, setSubmitting] = useState(false);
    const [createError, setCreateError] = useState<string | null>(null);

    const load = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const activities = await jobActivitiesApi.list({
                search: filters.search.trim() || undefined,
                searchType: filters.searchType || undefined,
                jobType: filters.jobType || undefined,
                status: filters.status || undefined
            });
            setEntries(activities);
        } catch (cause) {
            setEntries([]);
            setError(cause instanceof Error ? cause.message : "Failed to load job activities");
        } finally {
            setLoading(false);
        }
    }, [filters]);

    useEffect(() => {
        const timeout = setTimeout(() => load(), 300);
        return () => clearTimeout(timeout);
    }, [load]);

    const canCreate = [form.searchType, form.jobType, form.companyName, form.status].every(Boolean) && !submitting;

    const handleSubmit = async () => {
        if (!canCreate) {
            return;
        }
        setSubmitting(true);
        setCreateError(null);
        try {
            await jobActivitiesApi.create({
                activityDate: form.activityDate.toISOString(),
                searchType: form.searchType,
                jobType: form.jobType,
                companyName: form.companyName,
                status: form.status
            });
            setCreateOpen(false);
            setForm(createEmptyForm());
            await load();
        } catch (cause) {
            setCreateError(cause instanceof Error ? cause.message : "Failed to create entry");
        } finally {
            setSubmitting(false);
        }
    };

    const openFilters = () => {
        setDraft({...filters});
        setFilterModalOpen(true);
    };

    const applyFilters = () => {
        setFilters({...draft});
        setFilterModalOpen(false);
    };

    const activeFilterCount = Object.values(filters).filter(value => value.trim() !== "").length;

    return (
        <div className="test-page">
            <div className="test-header">
                <h2 className="test-header__title">Jobs</h2>
                <Button onClick={() => setCreateOpen(true)}>New entry</Button>
            </div>

            <div className="test-toolbar">
                <Button onClick={openFilters}>
                    {activeFilterCount > 0 ? `Filters (${activeFilterCount})` : "Filters"}
                </Button>
            </div>

            <div className="test-list">
                {loading && <p className="test-list__notice">Loading...</p>}
                {!loading && error && <p className="test-list__notice test-list__notice--error">{error}</p>}
                {!loading && !error && entries.length === 0 && (
                    <p className="test-list__notice">No activities found.</p>
                )}
                {entries.length > 0 && (
                    <table className="test-table">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Search type</th>
                                <th>Job type</th>
                                <th>Company</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            {entries.map(entry => (
                                <tr key={entry.id}>
                                    <td>{new Date(entry.activityDate).toLocaleDateString()}</td>
                                    <td>{entry.searchType}</td>
                                    <td>{entry.jobType}</td>
                                    <td>{entry.companyName}</td>
                                    <td>{entry.status}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            <Modal open={filterModalOpen} onClose={() => setFilterModalOpen(false)} title="Filters">
                <div className="test-form">
                    <label className="test-form__field">
                        <span className="test-form__label">Search</span>
                        <Input
                            type="search"
                            placeholder="Company, job type, status..."
                            value={draft.search}
                            onChange={value => setDraft(draft => ({...draft, search: value}))}
                        />
                    </label>
                    <label className="test-form__field">
                        <span className="test-form__label">Search type</span>
                        <SelectableInput
                            placeholder="All search types"
                            setValue={value => setDraft(draft => ({...draft, searchType: value}))}
                            fitContent
                        >
                            {SEARCH_TYPES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </label>
                    <label className="test-form__field">
                        <span className="test-form__label">Job type</span>
                        <SelectableInput
                            placeholder="All job types"
                            setValue={value => setDraft(draft => ({...draft, jobType: value}))}
                            fitContent
                        >
                            {JOB_TYPES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </label>
                    <label className="test-form__field">
                        <span className="test-form__label">Status</span>
                        <SelectableInput
                            placeholder="All statuses"
                            setValue={value => setDraft(draft => ({...draft, status: value}))}
                            fitContent
                        >
                            {STATUSES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </label>
                    <div className="test-form__actions test-form__actions--between">
                        <Button onClick={() => setDraft(createEmptyFilters())}>Clear</Button>
                        <div className="test-form__actions">
                            <Button onClick={() => setFilterModalOpen(false)}>Cancel</Button>
                            <Button onClick={applyFilters}>Apply</Button>
                        </div>
                    </div>
                </div>
            </Modal>

            <Modal open={createOpen} onClose={() => setCreateOpen(false)} title="New entry">
                <div className="test-form">
                    <label className="test-form__field">
                        <span className="test-form__label">Date of activity</span>
                        <CalendarSelector
                            value={form.activityDate}
                            setValue={value => setForm(form => ({...form, activityDate: value}))}
                        />
                    </label>
                    <label className="test-form__field">
                        <span className="test-form__label">Search type</span>
                        <SelectableInput
                            placeholder="Select a search type"
                            setValue={value => setForm(form => ({...form, searchType: value}))}
                        >
                            {SEARCH_TYPES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </label>
                    <label className="test-form__field">
                        <span className="test-form__label">Job type</span>
                        <SelectableInput
                            placeholder="Select a job type"
                            setValue={value => setForm(form => ({...form, jobType: value}))}
                        >
                            {JOB_TYPES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </label>
                    <label className="test-form__field">
                        <span className="test-form__label">Company name</span>
                        <Input
                            type="text"
                            placeholder="Acme Inc."
                            value={form.companyName}
                            onChange={value => setForm(form => ({...form, companyName: value}))}
                        />
                    </label>
                    <label className="test-form__field">
                        <span className="test-form__label">Status</span>
                        <SelectableInput
                            placeholder="Select a status"
                            setValue={value => setForm(form => ({...form, status: value}))}
                        >
                            {STATUSES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </label>
                    {createError && <p className="test-form__error">{createError}</p>}
                    <div className="test-form__actions">
                        <Button onClick={() => setCreateOpen(false)}>Cancel</Button>
                        <Button disabled={!canCreate} onClick={handleSubmit}>
                            {submitting ? "Creating..." : "Create"}
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default TestPage;