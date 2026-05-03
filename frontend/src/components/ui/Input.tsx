interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  error?: string
}

export function Input({ error, className = '', ...props }: InputProps) {
  return (
    <div className="flex flex-col gap-1">
      <input
        className={[
          'w-full rounded-lg border px-3 py-2.5 text-sm text-gray-900 placeholder-gray-400',
          'outline-none transition-colors focus:ring-2 focus:ring-offset-0',
          error
            ? 'border-red-400 focus:border-red-400 focus:ring-red-200'
            : 'border-gray-300 focus:border-blue-500 focus:ring-blue-100',
          className,
        ].join(' ')}
        {...props}
      />
      {error && <p className="text-xs text-red-600">{error}</p>}
    </div>
  )
}
