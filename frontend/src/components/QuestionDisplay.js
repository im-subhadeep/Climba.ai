import React from 'react';
import './QuestionDisplay.css';
import QuestionCard from './QuestionCard';

function QuestionDisplay({ questions, showToast }) {
  const downloadQuestions = () => {
    const content = formatQuestionsForDownload(questions);
    const blob = new Blob([content], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'interview-questions.txt';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    if (showToast) {
      showToast('Questions downloaded successfully!', 'success');
    }
  };

  const formatQuestionsForDownload = (questions) => {
    let content = 'INTERVIEW QUESTIONS\n';
    content += '='.repeat(50) + '\n\n';

    content += 'TECHNICAL QUESTIONS\n';
    content += '-'.repeat(50) + '\n';
    questions.technicalQuestions.forEach((q, index) => {
      content += `${index + 1}. ${q.question}\n`;
      if (q.answer) {
        content += `   Answer: ${q.answer}\n`;
      }
      content += '\n';
    });

    content += '\nBEHAVIORAL QUESTIONS\n';
    content += '-'.repeat(50) + '\n';
    questions.behavioralQuestions.forEach((q, index) => {
      content += `${index + 1}. ${q.question}\n`;
      if (q.answer) {
        content += `   Answer: ${q.answer}\n`;
      }
      content += '\n';
    });

    return content;
  };

  const totalQuestions = (questions.technicalQuestions?.length || 0) + (questions.behavioralQuestions?.length || 0);

  return (
    <div className="question-display">
      <div className="display-header">
        <div className="header-info">
          <h2>Generated Questions</h2>
          <span className="question-count">{totalQuestions} questions generated</span>
        </div>
        <button onClick={downloadQuestions} className="download-button">
          <span className="download-icon">📥</span>
          Download All
        </button>
      </div>

      <div className="questions-section">
        <div className="section">
          <h3 className="section-title">
            <span className="section-icon">💻</span>
            Technical Questions
            <span className="section-count">({questions.technicalQuestions?.length || 0})</span>
          </h3>
          <div className="questions-grid">
            {questions.technicalQuestions?.map((q, index) => (
              <QuestionCard
                key={index}
                question={q.question}
                answer={q.answer}
                type="technical"
                index={index + 1}
                showToast={showToast}
              />
            ))}
          </div>
        </div>

        <div className="section">
          <h3 className="section-title">
            <span className="section-icon">🧠</span>
            Behavioral Questions
            <span className="section-count">({questions.behavioralQuestions?.length || 0})</span>
          </h3>
          <div className="questions-grid">
            {questions.behavioralQuestions?.map((q, index) => (
              <QuestionCard
                key={index}
                question={q.question}
                answer={q.answer}
                type="behavioral"
                index={index + 1}
                showToast={showToast}
              />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default QuestionDisplay;
