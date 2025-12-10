import React, { useState, useEffect } from 'react';
import './Toast.css';

function Toast({ message, type = 'success', onClose, duration = 3000 }) {
    const [isVisible, setIsVisible] = useState(true);

    useEffect(() => {
        const timer = setTimeout(() => {
            setIsVisible(false);
            setTimeout(onClose, 300); // Wait for fade out animation
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, onClose]);

    const icons = {
        success: '✓',
        error: '✕',
        info: 'ℹ'
    };

    return (
        <div className={`toast ${type} ${isVisible ? 'visible' : 'hidden'}`}>
            <span className="toast-icon">{icons[type]}</span>
            <span className="toast-message">{message}</span>
        </div>
    );
}

export default Toast;
