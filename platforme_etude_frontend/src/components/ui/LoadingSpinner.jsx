import './index.css';

export default function LoadingSpinner({ size = 'md', text }) {
  const sizes = { sm: 20, md: 32, lg: 48 };
  const s = sizes[size] || sizes.md;

  return (
    <div className="loading-spinner-container">
      <svg
        width={s}
        height={s}
        viewBox="0 0 24 24"
        fill="none"
        className="loading-spinner-svg"
      >
        <circle
          cx="12"
          cy="12"
          r="10"
          stroke="var(--border)"
          strokeWidth="3"
          fill="none"
        />
        <path
          d="M12 2a10 10 0 0 1 10 10"
          stroke="var(--primary)"
          strokeWidth="3"
          strokeLinecap="round"
          fill="none"
        />
      </svg>
      {text && <p className="loading-spinner-text">{text}</p>}
    </div>
  );
}
