import {useState} from "react";
import "./CalendarSelector.css";

export interface CalendarSelectorProps {
    value?: Date;
    setValue: (date: Date) => void;
}

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

const MONTHS = Array.from({length: 12}, (_, index) =>
    new Date(2000, index, 1).toLocaleDateString(undefined, {month: "short"})
);

const getMonthCells = (year: number, month: number): Date[] => {
    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const prevMonthDays = new Date(year, month, 0).getDate();
    const cells: Date[] = [];
    for (let i = firstDay - 1; i >= 0; i--) {
        cells.push(new Date(year, month - 1, prevMonthDays - i));
    }
    for (let day = 1; day <= daysInMonth; day++) {
        cells.push(new Date(year, month, day));
    }
    let nextDay = 1;
    while (cells.length % 7 !== 0) {
        cells.push(new Date(year, month + 1, nextDay++));
    }
    return cells;
};

const CalendarSelector = ({value, setValue}: CalendarSelectorProps) => {
    const today = new Date();
    const [picker, setPicker] = useState<"month" | "year" | null>(null);
    const [viewedMonth, setViewedMonth] = useState(
        () => value
            ? new Date(value.getFullYear(), value.getMonth(), 1)
            : new Date(today.getFullYear(), today.getMonth(), 1)
    );

    const year = viewedMonth.getFullYear();
    const month = viewedMonth.getMonth();
    const todayKey = today.toDateString();
    const selectedKey = value?.toDateString();
    const monthLabel = viewedMonth.toLocaleDateString(undefined, {month: "long"});
    const cells = getMonthCells(year, month);
    const yearWindowStart = year - (year % 12);
    const yearWindow = Array.from({length: 12}, (_, index) => yearWindowStart + index);

    const handlePrev = () => {
        if (picker === "year") {
            setViewedMonth(new Date(year - 12, month, 1));
        } else {
            setViewedMonth(new Date(year, month - 1, 1));
        }
    };

    const handleNext = () => {
        if (picker === "year") {
            setViewedMonth(new Date(year + 12, month, 1));
        } else {
            setViewedMonth(new Date(year, month + 1, 1));
        }
    };

    const selectDate = (date: Date) => {
        setValue(date);
        setViewedMonth(new Date(date.getFullYear(), date.getMonth(), 1));
    };

    const togglePicker = (mode: "month" | "year") =>
        setPicker(current => (current === mode ? null : mode));

    return (
        <div className="calendar">
            <div className="calendar__header">
                <button type="button" className="calendar__nav" onClick={handlePrev} aria-label="Previous">
                    {"\u2039"}
                </button>
                <div className="calendar__label-group">
                    <button
                        type="button"
                        className={`calendar__label${picker === "month" ? " calendar__label--active" : ""}`}
                        onClick={() => togglePicker("month")}
                    >
                        {monthLabel}
                    </button>
                    <button
                        type="button"
                        className={`calendar__label${picker === "year" ? " calendar__label--active" : ""}`}
                        onClick={() => togglePicker("year")}
                    >
                        {year}
                    </button>
                </div>
                <button type="button" className="calendar__nav" onClick={handleNext} aria-label="Next">
                    {"\u203A"}
                </button>
            </div>
            {picker === "month" && (
                <div className="calendar__picker">
                    {MONTHS.map((label, index) => (
                        <button
                            type="button"
                            key={label}
                            className={[
                                "calendar__choice",
                                index === month ? "calendar__choice--selected" : "",
                                index === today.getMonth() && year === today.getFullYear() ? "calendar__choice--today" : ""
                            ].filter(Boolean).join(" ")}
                            onClick={() => {
                                setViewedMonth(new Date(year, index, 1));
                                setPicker(null);
                            }}
                        >
                            {label}
                        </button>
                    ))}
                </div>
            )}
            {picker === "year" && (
                <div className="calendar__picker">
                    {yearWindow.map(candidate => (
                        <button
                            type="button"
                            key={candidate}
                            className={[
                                "calendar__choice",
                                candidate === year ? "calendar__choice--selected" : "",
                                candidate === today.getFullYear() ? "calendar__choice--today" : ""
                            ].filter(Boolean).join(" ")}
                            onClick={() => {
                                setViewedMonth(new Date(candidate, month, 1));
                                setPicker(null);
                            }}
                        >
                            {candidate}
                        </button>
                    ))}
                </div>
            )}
            {picker === null && (
                <>
                    <div className="calendar__weekdays">
                        {WEEKDAYS.map(day => (
                            <span className="calendar__weekday" key={day}>{day}</span>
                        ))}
                    </div>
                    <div className="calendar__grid">
                        {cells.map((date, index) => {
                            const inCurrentMonth = date.getFullYear() === year && date.getMonth() === month;
                            const isToday = date.toDateString() === todayKey;
                            const isSelected = date.toDateString() === selectedKey;
                            const classes = [
                                "calendar__day",
                                inCurrentMonth ? "" : "calendar__day--adjacent",
                                isToday ? "calendar__day--today" : "",
                                isSelected ? "calendar__day--selected" : ""
                            ].filter(Boolean).join(" ");
                            return (
                                <button
                                    type="button"
                                    className={classes}
                                    key={index}
                                    onClick={() => selectDate(date)}
                                >
                                    {date.getDate()}
                                </button>
                            );
                        })}
                    </div>
                </>
            )}
        </div>
    );
};

export default CalendarSelector;