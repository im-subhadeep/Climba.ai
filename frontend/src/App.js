import React, { useState, useEffect } from 'react';
import './App.css';
import QuestionForm from './components/QuestionForm';
import QuestionDisplay from './components/QuestionDisplay';
import LoadingSpinner from './components/LoadingSpinner';
import DarkModeToggle from './components/DarkModeToggle';
import Toast from './components/Toast';
import HistorySidebar from './components/HistorySidebar';

function App() {
  const [questions, setQuestions] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [toast, setToast] = useState(null);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);

  // Initialize dark mode from localStorage
  useEffect(() => {
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
      setIsDarkMode(true);
      document.documentElement.setAttribute('data-theme', 'dark');
    }
  }, []);

  // Toggle dark mode
  const toggleDarkMode = () => {
    setIsDarkMode(prev => {
      const newValue = !prev;
      document.documentElement.setAttribute('data-theme', newValue ? 'dark' : 'light');
      localStorage.setItem('theme', newValue ? 'dark' : 'light');
      return newValue;
    });
  };

  // Show toast notification
  const showToast = (message, type = 'success') => {
    setToast({ message, type });
  };

  const handleGenerate = async (formData) => {
    setLoading(true);
    setError(null);
    setQuestions(null);

    try {
      const response = await fetch('http://localhost:8080/api/questions/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(formData),
      });

      if (!response.ok) {
        throw new Error('Failed to generate questions');
      }

      const data = await response.json();
      setQuestions(data);
      showToast(`Generated ${data.technicalQuestions?.length || 0} technical and ${data.behavioralQuestions?.length || 0} behavioral questions!`, 'success');
    } catch (err) {
      setError(err.message || 'An error occurred while generating questions');
      showToast('Failed to generate questions. Please try again.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleLoadHistory = (historyQuestions) => {
    setQuestions(historyQuestions);
    setError(null);
  };

  return (
    <div className="App" data-theme={isDarkMode ? 'dark' : 'light'}>
      <DarkModeToggle isDark={isDarkMode} onToggle={toggleDarkMode} />

      {/* History Button */}
      <button
        className="history-toggle-button"
        onClick={() => setIsHistoryOpen(true)}
        title="View History"
      >
        📚 History
      </button>

      <HistorySidebar
        isOpen={isHistoryOpen}
        onClose={() => setIsHistoryOpen(false)}
        onLoadHistory={handleLoadHistory}
        showToast={showToast}
      />

      <div className="container">
        <header className="header">
          <h1>Interview Question Generator</h1>
          <p>Generate tailored interview questions for any role and topic</p>
        </header>

        <QuestionForm onGenerate={handleGenerate} loading={loading} />

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        {loading && <LoadingSpinner />}

        {questions && !loading && (
          <QuestionDisplay questions={questions} showToast={showToast} />
        )}
      </div>

      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={() => setToast(null)}
        />
      )}
    </div>
  );
}

export default App;
