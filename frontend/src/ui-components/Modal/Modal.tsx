import {useEffect, type ReactNode} from "react";
import {createPortal} from "react-dom";
import "./Modal.css";

export interface ModalProps {
    open: boolean;
    onClose: () => void;
    title?: string;
    children: ReactNode;
}

const Modal = ({open, onClose, title, children}: ModalProps) => {
    useEffect(() => {
        if (!open) {
            return;
        }
        const handleKeyDown = (event: KeyboardEvent) => {
            if (event.key === "Escape") {
                onClose();
            }
        };
        document.addEventListener("keydown", handleKeyDown);
        const previousOverflow = document.body.style.overflow;
        document.body.style.overflow = "hidden";
        return () => {
            document.removeEventListener("keydown", handleKeyDown);
            document.body.style.overflow = previousOverflow;
        };
    }, [open, onClose]);

    if (!open) {
        return null;
    }

    return createPortal(
        <div className="modal-overlay" onClick={onClose}>
            <div className="modal" role="dialog" aria-modal="true" onClick={event => event.stopPropagation()}>
                <div className="modal__header">
                    {title && <h2 className="modal__title">{title}</h2>}
                    <button type="button" className="modal__close" onClick={onClose} aria-label="Close">
                        {"\u00D7"}
                    </button>
                </div>
                <div className="modal__body">{children}</div>
            </div>
        </div>,
        document.body
    );
};

export default Modal;