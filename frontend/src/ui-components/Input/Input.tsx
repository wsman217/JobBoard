import {forwardRef, type InputHTMLAttributes} from "react";
import "./Input.css";

export interface InputProps
    extends Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "value"> {
    value?: string;
    onChange?: (value: string) => void;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
    ({value, onChange, className, style, ...rest}, ref) => (
        <input
            ref={ref}
            className={className ? `input ${className}` : "input"}
            style={style}
            value={value}
            onChange={event => onChange?.(event.target.value)}
            {...rest}
        />
    )
);

Input.displayName = "Input";

export default Input;