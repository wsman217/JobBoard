import {
    Children,
    cloneElement,
    useEffect,
    useLayoutEffect,
    useMemo,
    useRef,
    useState,
    type KeyboardEventHandler,
    type ReactElement,
    type ReactNode
} from "react";
import type {SelectableInputOptionProps} from "./SelectableInputOption";
import "./SelectableInput.css";

const INPUT_WIDTH_OFFSET = 24;

export interface SelectableInputProps {
    placeholder?: string;
    setValue: (value: string) => void;
    maxWidth?: number;
    children: ReactNode;
}

const SelectableInput = ({placeholder, setValue, maxWidth, children}: SelectableInputProps) => {
    const [isOpen, setIsOpen] = useState(false);
    const [text, setText] = useState("");
    const [measuredWidth, setMeasuredWidth] = useState<number | undefined>();
    const [activeIndex, setActiveIndex] = useState(0);
    const wrapperRef = useRef<HTMLDivElement>(null);
    const measureRef = useRef<HTMLSpanElement>(null);
    const listRef = useRef<HTMLUListElement>(null);

    const options = useMemo(
        () => Children.toArray(children) as ReactElement<SelectableInputOptionProps>[],
        [children]
    );

    const longestId = useMemo(
        () => options.reduce(
            (longest, option) => (option.props.id.length > longest.length ? option.props.id : longest),
            ""
        ),
        [options]
    );

    const filteredOptions = useMemo(() => {
        const query = text.trim().toLowerCase();
        const isExactMatch = options.some(option => option.props.id.toLowerCase() === query);
        if (isExactMatch) {
            return options;
        }
        return options.filter(option => option.props.id.toLowerCase().includes(query));
    }, [options, text]);

    useLayoutEffect(() => {
        if (measureRef.current) {
            setMeasuredWidth(measureRef.current.offsetWidth);
        }
    }, [longestId]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    useEffect(() => {
        const activeElement = listRef.current?.children[activeIndex] as HTMLElement | undefined;
        activeElement?.scrollIntoView({block: "nearest"});
    }, [activeIndex, isOpen, filteredOptions]);

    const handleChange = (displayValue: string, dataValue: string) => {
        setText(displayValue);
        setValue(dataValue);
        setActiveIndex(0);
        setIsOpen(true);
    };

    const handleSelect = (displayValue: string, dataValue: string) => {
        handleChange(displayValue, dataValue);
        setIsOpen(false);
    };

    const handleClick = () => {
        setActiveIndex(0);
        setIsOpen(true);
    };

    const handleKeyDown: KeyboardEventHandler<HTMLInputElement> = event => {
        const {key} = event;
        if (key === "ArrowDown" || key === "ArrowUp") {
            event.preventDefault();
            if (!isOpen) {
                handleClick();
                return;
            }
            const delta = key === "ArrowDown" ? 1 : -1;
            setActiveIndex(prev => (prev + delta + filteredOptions.length) % filteredOptions.length);
        } else if (key === "Enter" && isOpen && filteredOptions.length > 0) {
            event.preventDefault();
            const active = filteredOptions[activeIndex] as ReactElement<SelectableInputOptionProps>;
            handleSelect(active.props.id, active.props.value ?? active.props.id);
        } else if (key === "Escape" && isOpen) {
            setIsOpen(false);
        }
    };

    return (
        <div ref={wrapperRef} className="selectable-input">
            <span ref={measureRef} className="selectable-input__measure">{longestId}</span>
            <input
                className="selectable-input__field"
                placeholder={placeholder}
                value={text}
                onClick={handleClick}
                onChange={event => handleChange(event.target.value, event.target.value)}
                onKeyDown={handleKeyDown}
                style={{
                    width: measuredWidth !== undefined ? measuredWidth + INPUT_WIDTH_OFFSET : undefined,
                    maxWidth
                }}
            />
            {isOpen && filteredOptions.length > 0 && (
                <ul ref={listRef} className="selectable-input__options"
                    style={maxWidth !== undefined ? {maxWidth} : undefined}>
                    {filteredOptions.map((option, index) =>
                        cloneElement(option, {
                            onClick: handleSelect,
                            selected: index === activeIndex
                        })
                    )}
                </ul>
            )}
        </div>
    );
};

export default SelectableInput;