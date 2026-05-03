import DatePicker from 'react-datepicker'
import 'react-datepicker/dist/react-datepicker.css'

interface FrequenciaDatePickerProps {
  selected: Date
  onChange: (date: Date) => void
  hasRegistro?: boolean
  disabled?: boolean
}

export function FrequenciaDatePicker({
  selected,
  onChange,
  hasRegistro,
  disabled,
}: FrequenciaDatePickerProps) {
  return (
    <div className="flex flex-col gap-1.5">
      <label className="text-sm font-medium text-gray-700">Data da aula</label>
      <div className="relative flex items-center gap-2">
        <DatePicker
          selected={selected}
          onChange={date => { if (date) onChange(date) }}
          dateFormat="dd/MM/yyyy"
          maxDate={new Date()}
          disabled={disabled}
          className="w-40 rounded-lg border border-gray-300 px-3 py-2 text-sm text-gray-900 outline-none transition-colors focus:border-blue-500 focus:ring-2 focus:ring-blue-100 disabled:opacity-50"
          wrapperClassName="w-auto"
        />
        {hasRegistro && (
          <span className="inline-flex items-center gap-1 rounded-full bg-blue-100 px-2.5 py-1 text-xs font-medium text-blue-700">
            <svg className="h-3 w-3" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
            </svg>
            Frequência já lançada
          </span>
        )}
      </div>
    </div>
  )
}
