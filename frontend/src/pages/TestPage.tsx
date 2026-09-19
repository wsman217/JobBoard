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
    const [editingId, setEditingId] = useState<string | null>(null);
    const [menuOpenFor, setMenuOpenFor] = useState<string | null>(null);
    const [form, setForm] = useState<FormState>(createEmptyForm);
    const [submitting, setSubmitting] = useState(false);
    const [createError, setCreateError] = useState<string | null>(null);
    const [actionError, setActionError] = useState<string | null>(null);

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

    useEffect(() => {
        if (!menuOpenFor) {
            return;
        }
        const handleClickOutside = (event: MouseEvent) => {
            const target = event.target as Element | null;
            if (target && !target.closest(".row-actions")) {
                setMenuOpenFor(null);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [menuOpenFor]);

    const canCreate = [form.searchType, form.jobType, form.companyName, form.status].every(Boolean) && !submitting;

    const handleSubmit = async () => {
        if (!canCreate) {
            return;
        }
        setSubmitting(true);
        setCreateError(null);
        const payload = {
            activityDate: form.activityDate.toISOString(),
            searchType: form.searchType,
            jobType: form.jobType,
            companyName: form.companyName,
            status: form.status
        };
        try {
            if (editingId) {
                await jobActivitiesApi.update(editingId, payload);
            } else {
                await jobActivitiesApi.create(payload);
            }
            closeForm();
            await load();
        } catch (cause) {
            setCreateError(
                cause instanceof Error ? cause.message
                    : editingId ? "Failed to update entry" : "Failed to create entry"
            );
        } finally {
            setSubmitting(false);
        }
    };

    const openCreate = () => {
        setEditingId(null);
        setCreateError(null);
        setForm(createEmptyForm());
        setCreateOpen(true);
    };

    const openEdit = (entry: JobActivity) => {
        setEditingId(entry.id);
        setCreateError(null);
        setForm({
            activityDate: new Date(entry.activityDate),
            searchType: entry.searchType,
            jobType: entry.jobType,
            companyName: entry.companyName,
            status: entry.status
        });
        setCreateOpen(true);
        setMenuOpenFor(null);
    };

    const closeForm = () => {
        setCreateOpen(false);
        setEditingId(null);
        setForm(createEmptyForm());
        setCreateError(null);
    };

    const handleDelete = async (id: string) => {
        setMenuOpenFor(null);
        setActionError(null);
        try {
            await jobActivitiesApi.delete(id);
            await load();
        } catch (cause) {
            setActionError(cause instanceof Error ? cause.message : "Failed to delete entry");
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
                <Button onClick={openCreate}>New entry</Button>
            </div>

            <div className="test-toolbar">
                <Button onClick={openFilters}>
                    {activeFilterCount > 0 ? `Filters (${activeFilterCount})` : "Filters"}
                </Button>
            </div>

            {actionError && <p className="test-list__notice test-list__notice--error">{actionError}</p>}

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
                                <th aria-label="Actions" />
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
                                    <td className="test-table__actions">
                                        <div className="row-actions">
                                            <button
                                                type="button"
                                                className="row-actions__button"
                                                aria-label="Row actions"
                                                aria-expanded={menuOpenFor === entry.id || undefined}
                                                onClick={() => setMenuOpenFor(menuOpenFor === entry.id ? null : entry.id)}
                                            >
                                                {"\u22EE"}
                                            </button>
                                            {menuOpenFor === entry.id && (
                                                <div className="row-menu">
                                                    <button type="button" className="row-menu__item" onClick={() => openEdit(entry)}>
                                                        Edit
                                                    </button>
                                                    <button
                                                        type="button"
                                                        className="row-menu__item row-menu__item--danger"
                                                        onClick={() => handleDelete(entry.id)}
                                                    >
                                                        Delete
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            <Modal open={filterModalOpen} onClose={() => setFilterModalOpen(false)} title="Filters">
                <div className="test-form">
                    <div className="test-form__field">
                        <Input
                            label="Search"
                            type="search"
                            placeholder="Company, job type, status..."
                            value={draft.search}
                            onChange={value => setDraft(draft => ({...draft, search: value}))}
                        />
                    </div>
                    <div className="test-form__field">
                        <SelectableInput
                            label="Search type"
                            placeholder="All search types"
                            value={draft.searchType}
                            setValue={value => setDraft(draft => ({...draft, searchType: value}))}
                            fitContent
                        >
                            {SEARCH_TYPES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </div>
                    <div className="test-form__field">
                        <SelectableInput
                            label="Job type"
                            placeholder="All job types"
                            value={draft.jobType}
                            setValue={value => setDraft(draft => ({...draft, jobType: value}))}
                            fitContent
                        >
                            {JOB_TYPES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </div>
                    <div className="test-form__field">
                        <SelectableInput
                            label="Status"
                            placeholder="All statuses"
                            value={draft.status}
                            setValue={value => setDraft(draft => ({...draft, status: value}))}
                            fitContent
                        >
                            {STATUSES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </div>
                    <div className="test-form__actions test-form__actions--between">
                        <Button onClick={() => setDraft(createEmptyFilters())}>Clear</Button>
                        <div className="test-form__actions">
                            <Button onClick={() => setFilterModalOpen(false)}>Cancel</Button>
                            <Button onClick={applyFilters}>Apply</Button>
                        </div>
                    </div>
                </div>
            </Modal>

            <Modal open={createOpen} onClose={closeForm} title={editingId ? "Edit entry" : "New entry"}>
                <div className="test-form">
                    <label className="test-form__field">
                        <span className="test-form__label">Date of activity</span>
                        <CalendarSelector
                            value={form.activityDate}
                            setValue={value => setForm(form => ({...form, activityDate: value}))}
                        />
                    </label>
                    <div className="test-form__field">
                        <SelectableInput
                            label="Search type"
                            placeholder="Select a search type"
                            value={form.searchType}
                            setValue={value => setForm(form => ({...form, searchType: value}))}
                        >
                            {SEARCH_TYPES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </div>
                    <div className="test-form__field">
                        <SelectableInput
                            label="Job type"
                            placeholder="Select a job type"
                            value={form.jobType}
                            setValue={value => setForm(form => ({...form, jobType: value}))}
                        >
                            {JOB_TYPES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </div>
                    <div className="test-form__field">
                        <Input
                            label="Company name"
                            type="text"
                            placeholder="Acme Inc."
                            value={form.companyName}
                            onChange={value => setForm(form => ({...form, companyName: value}))}
                        />
                    </div>
                    <div className="test-form__field">
                        <SelectableInput
                            label="Status"
                            placeholder="Select a status"
                            value={form.status}
                            setValue={value => setForm(form => ({...form, status: value}))}
                        >
                            {STATUSES.map(id => <SelectableInputOption key={id} id={id} />)}
                        </SelectableInput>
                    </div>
                    {createError && <p className="test-form__error">{createError}</p>}
                    <div className="test-form__actions">
                        <Button onClick={closeForm}>Cancel</Button>
                        <Button disabled={!canCreate} onClick={handleSubmit}>
                            {submitting ? (editingId ? "Saving..." : "Creating...") : editingId ? "Save" : "Create"}
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};

export default TestPage;