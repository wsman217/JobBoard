export interface SelectableInputOptionProps {
    id: string;
    value?: string;
    onClick?: (id: string, value: string) => void;
    selected?: boolean;
}

const SelectableInputOption = ({id, value, onClick, selected}: SelectableInputOptionProps) => {
    const select = () => onClick?.(id, value ?? id);

    return (
        <li onClick={select} data-selected={selected || undefined}>
            {id}
        </li>
    );
};

export default SelectableInputOption;