import React, { useState } from 'react';
import './QuestionForm.css';

function QuestionForm({ onGenerate, loading }) {
  const [formData, setFormData] = useState({
    role: '',
    topic: '',
    difficulty: 'medium',
    includeAnswers: false,
  });

  const difficultyInfo = {
    easy: {
      label: 'Easy',
      description: 'Basic concepts and fundamentals. Ideal for entry-level positions.',
      temperature: '0.6 - More focused responses'
    },
    medium: {
      label: 'Medium',
      description: 'Intermediate concepts with practical applications. Suitable for mid-level roles.',
      temperature: '0.7 - Balanced creativity'
    },
    hard: {
      label: 'Hard',
      description: 'Advanced concepts, system design, and problem-solving. For senior positions.',
      temperature: '0.8 - More creative and varied'
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (formData.role.trim() && formData.topic.trim()) {
      onGenerate(formData);
    }
  };

  return (
    <form className="question-form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="role">Job Role *</label>
        <input
          type="text"
          id="role"
          name="role"
          value={formData.role}
          onChange={handleChange}
          placeholder="e.g., Software Engineer, Data Scientist"
          required
          disabled={loading}
        />
      </div>

      <div className="form-group">
        <label htmlFor="topic">Topic *</label>
        <input
          type="text"
          id="topic"
          name="topic"
          value={formData.topic}
          onChange={handleChange}
          placeholder="e.g., Java, Machine Learning, System Design"
          required
          disabled={loading}
        />
      </div>

      <div className="form-group">
        <label htmlFor="difficulty">
          Difficulty Level *
          <span className="info-icon" title="Hover over options for details">ℹ️</span>
        </label>
        <div className="difficulty-selector">
          {Object.entries(difficultyInfo).map(([key, info]) => (
            <label
              key={key}
              className={`difficulty-option ${formData.difficulty === key ? 'selected' : ''}`}
            >
              <input
                type="radio"
                name="difficulty"
                value={key}
                checked={formData.difficulty === key}
                onChange={handleChange}
                disabled={loading}
              />
              <span className="difficulty-content">
                <span className="difficulty-label">{info.label}</span>
                <span className="difficulty-badge">{key === 'easy' ? '🟢' : key === 'medium' ? '🟡' : '🔴'}</span>
              </span>
              <div className="tooltip">
                <strong>{info.label}</strong>
                <p>{info.description}</p>
                <span className="temp-info">AI Temperature: {info.temperature}</span>
              </div>
            </label>
          ))}
        </div>
      </div>

      <div className="form-group checkbox-group">
        <label htmlFor="includeAnswers" className="checkbox-label">
          <input
            type="checkbox"
            id="includeAnswers"
            name="includeAnswers"
            checked={formData.includeAnswers}
            onChange={handleChange}
            disabled={loading}
          />
          <span className="checkbox-custom"></span>
          <span>Include sample answers</span>
          <span className="checkbox-hint">AI will generate suggested answers for each question</span>
        </label>
      </div>

      <button
        type="submit"
        className="generate-button"
        disabled={loading || !formData.role.trim() || !formData.topic.trim()}
      >
        {loading ? (
          <>
            <span className="button-spinner"></span>
            Generating...
          </>
        ) : (
          <>
            <span className="button-icon">✨</span>
            Generate Questions
          </>
        )}
      </button>
    </form>
  );
}

export default QuestionForm;
