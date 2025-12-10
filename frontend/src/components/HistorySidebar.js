import React, { useState, useEffect } from 'react';
import './HistorySidebar.css';

function HistorySidebar({ onLoadHistory, isOpen, onClose, showToast }) {
    const [history, setHistory] = useState([]);
    const [loading, setLoading] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');

    useEffect(() => {
        if (isOpen) {
            fetchHistory();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isOpen]);

    const fetchHistory = async () => {
        setLoading(true);
        try {
            const response = await fetch('http://localhost:8080/api/history');
            if (response.ok) {
                const data = await response.json();
                setHistory(data);
            }
        } catch (error) {
            console.error('Failed to fetch history:', error);
            if (showToast) {
                showToast('Failed to load history', 'error');
            }
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id, e) => {
        e.stopPropagation();
        try {
            const response = await fetch(`http://localhost:8080/api/history/${id}`, {
                method: 'DELETE'
            });
            if (response.ok) {
                setHistory(history.filter(h => h.id !== id));
                if (showToast) {
                    showToast('History item deleted', 'success');
                }
            }
        } catch (error) {
            console.error('Failed to delete:', error);
        }
    };

    const handleLoad = (item) => {
        onLoadHistory({
            technicalQuestions: item.technicalQuestions,
            behavioralQuestions: item.behavioralQuestions
        });
        if (showToast) {
            showToast('Previous questions loaded!', 'success');
        }
        onClose();
    };

    const filteredHistory = history.filter(item =>
        item.role.toLowerCase().includes(searchTerm.toLowerCase()) ||
        item.topic.toLowerCase().includes(searchTerm.toLowerCase())
    );

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getDifficultyEmoji = (difficulty) => {
        switch (difficulty?.toLowerCase()) {
            case 'easy': return '🟢';
            case 'medium': return '🟡';
            case 'hard': return '🔴';
            default: return '⚪';
        }
    };

    return (
        <>
            <div className={`history-overlay ${isOpen ? 'visible' : ''}`} onClick={onClose}></div>
            <div className={`history-sidebar ${isOpen ? 'open' : ''}`}>
                <div className="sidebar-header">
                    <h2>📚 Question History</h2>
                    <button className="close-button" onClick={onClose}>✕</button>
                </div>

                <div className="search-box">
                    <input
                        type="text"
                        placeholder="Search by role or topic..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                    <span className="search-icon">🔍</span>
                </div>

                <div className="history-list">
                    {loading ? (
                        <div className="loading-state">
                            <div className="spinner-small"></div>
                            <p>Loading history...</p>
                        </div>
                    ) : filteredHistory.length === 0 ? (
                        <div className="empty-state">
                            <span className="empty-icon">📭</span>
                            <p>No history yet</p>
                            <span className="empty-hint">Generated questions will appear here</span>
                        </div>
                    ) : (
                        filteredHistory.map((item) => (
                            <div
                                key={item.id}
                                className="history-item"
                                onClick={() => handleLoad(item)}
                            >
                                <div className="item-header">
                                    <span className="item-role">{item.role}</span>
                                    <button
                                        className="delete-button"
                                        onClick={(e) => handleDelete(item.id, e)}
                                        title="Delete"
                                    >
                                        🗑️
                                    </button>
                                </div>
                                <div className="item-topic">{item.topic}</div>
                                <div className="item-meta">
                                    <span className="item-difficulty">
                                        {getDifficultyEmoji(item.difficulty)} {item.difficulty}
                                    </span>
                                    <span className="item-count">
                                        {item.technicalQuestions?.length || 0}T + {item.behavioralQuestions?.length || 0}B
                                    </span>
                                </div>
                                <div className="item-date">{formatDate(item.createdAt)}</div>
                            </div>
                        ))
                    )}
                </div>

                <div className="sidebar-footer">
                    <button className="refresh-button" onClick={fetchHistory} disabled={loading}>
                        🔄 Refresh
                    </button>
                    <span className="history-count">{history.length} items</span>
                </div>
            </div>
        </>
    );
}

export default HistorySidebar;
