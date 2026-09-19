import {forwardRef, type InputHTMLAttributes} from "react";
import "./Input.css";

export interface InputProps
    extends Omit<InputHTMLAttributes<HTMLInputElement>, "onChange" | "value"> {
    label?: string;
    value?: string;
    onChange?: (value: string) => void;
}

const Input = forwardRef<HTMLInputElement, InputProps>(
    ({value, onChange, className, style, label, ...rest}, ref) => {
        const input = (
            <input
                ref={ref}
                className={className ? `input ${className}` : "input"}
                style={style}
                value={value}
                onChange={event => onChange?.(event.target.value)}
                {...rest}
            />
        );

        if (!label) {
            return input;
        }

        return (
            <div className="input__group">
                <span className="input__label">{label}</span>
                {input}
            </div>
        );
    }
);

Input.displayName = "Input";

export default Input;