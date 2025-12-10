import React from 'react';
import './LoadingSpinner.css';

function LoadingSpinner() {
    return (
        <div className="loading-container">
            <div className="loading-content">
                <div className="spinner">
                    <div className="spinner-ring"></div>
                    <div className="spinner-ring"></div>
                    <div className="spinner-ring"></div>
                </div>
                <p className="loading-text">Generating your questions...</p>
                <p className="loading-subtext">This may take a few seconds</p>
            </div>

            {/* Skeleton Cards */}
            <div className="skeleton-section">
                <div className="skeleton-title"></div>
                <div className="skeleton-grid">
                    {[1, 2, 3, 4, 5].map((i) => (
                        <div key={i} className="skeleton-card">
                            <div className="skeleton-header">
                                <div className="skeleton-badge"></div>
                                <div className="skeleton-button"></div>
                            </div>
                            <div className="skeleton-line long"></div>
                            <div className="skeleton-line medium"></div>
                            <div className="skeleton-line short"></div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default LoadingSpinner;
