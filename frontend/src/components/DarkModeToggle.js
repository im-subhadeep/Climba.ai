import React from 'react';
import './DarkModeToggle.css';

function DarkModeToggle({ isDark, onToggle }) {
    return (
        <button
            className="dark-mode-toggle"
            onClick={onToggle}
            aria-label={isDark ? 'Switch to light mode' : 'Switch to dark mode'}
        >
            <div className={`toggle-track ${isDark ? 'dark' : 'light'}`}>
                <span className="toggle-icon sun">☀️</span>
                <span className="toggle-icon moon">🌙</span>
                <div className="toggle-thumb"></div>
            </div>
        </button>
    );
}

export default DarkModeToggle;
