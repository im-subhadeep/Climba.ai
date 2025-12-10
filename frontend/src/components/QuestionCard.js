import React, { useState } from 'react';
import './QuestionCard.css';

function QuestionCard({ question, answer, type, index, showToast }) {
  const [copied, setCopied] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);

  const handleCopy = () => {
    let textToCopy = question;
    if (answer) {
      textToCopy += `\n\nAnswer: ${answer}`;
    }

    navigator.clipboard.writeText(textToCopy).then(() => {
      setCopied(true);
      if (showToast) {
        showToast('Question copied to clipboard!', 'success');
      }
      setTimeout(() => setCopied(false), 2000);
    });
  };

  const toggleExpand = () => {
    if (answer) {
      setIsExpanded(!isExpanded);
    }
  };

  return (
    <div className={`question-card ${type} ${isExpanded ? 'expanded' : ''}`}>
      <div className="card-header">
        <span className="question-number">Q{index}</span>
        <div className="card-actions">
          {answer && (
            <button
              onClick={toggleExpand}
              className="expand-button"
              title={isExpanded ? 'Hide answer' : 'Show answer'}
            >
              {isExpanded ? '▲' : '▼'}
            </button>
          )}
          <button onClick={handleCopy} className={`copy-button ${copied ? 'copied' : ''}`}>
            {copied ? '✓ Copied' : '📋 Copy'}
          </button>
        </div>
      </div>
      <div className="card-content">
        <p className="question-text">{question}</p>
        {answer && (
          <div className={`answer-section ${isExpanded ? 'visible' : ''}`}>
            <h4 className="answer-label">
              <span className="answer-icon">💡</span>
              Sample Answer
            </h4>
            <p className="answer-text">{answer}</p>
          </div>
        )}
      </div>
      <div className="card-footer">
        <span className={`type-badge ${type}`}>
          {type === 'technical' ? '💻 Technical' : '🧠 Behavioral'}
        </span>
      </div>
    </div>
  );
}

export default QuestionCard;
