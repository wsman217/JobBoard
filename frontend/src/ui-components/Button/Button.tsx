import type {ReactNode} from "react";
import "./Button.css";

export interface ButtonProps {
    onClick: () => void;
    children: ReactNode;
    fontSize?: number;
    className?: string;
    disabled?: boolean;
}

const Button = ({onClick, children, fontSize, className, disabled}: ButtonProps) => {
    return (
        <button
            className={className ? `button ${className}` : "button"}
            style={fontSize !== undefined ? {fontSize} : undefined}
            onClick={onClick}
            disabled={disabled}
        >
            {children}
        </button>
    );
};

export default Button;