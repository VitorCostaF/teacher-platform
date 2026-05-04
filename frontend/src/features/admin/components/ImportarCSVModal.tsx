import { useEffect, useRef, useState } from 'react'
import Papa from 'papaparse'
import { Button } from '@/components/ui/Button'

interface ImportResult {
  importados: number
  erros: Array<{ linha: number; mensagem: string }>
}

interface Props {
  isOpen: boolean
  title: string
  instructions: string
  isLoading: boolean
  onClose: () => void
  onConfirm: (file: File) => Promise<ImportResult>
}

export function ImportarCSVModal({ isOpen, title, instructions, isLoading, onClose, onConfirm }: Props) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const [file, setFile] = useState<File | null>(null)
  const [headers, setHeaders] = useState<string[]>([])
  const [preview, setPreview] = useState<Record<string, string>[]>([])
  const [result, setResult] = useState<ImportResult | null>(null)

  useEffect(() => {
    const dialog = dialogRef.current
    if (!dialog) return
    if (isOpen) { if (!dialog.open) dialog.showModal() }
    else { if (dialog.open) dialog.close(); reset() }
  }, [isOpen])

  function reset() {
    setFile(null)
    setHeaders([])
    setPreview([])
    setResult(null)
  }

  function handleFile(e: React.ChangeEvent<HTMLInputElement>) {
    const f = e.target.files?.[0]
    if (!f) return
    setFile(f)
    setResult(null)
    Papa.parse<Record<string, string>>(f, {
      header: true,
      skipEmptyLines: true,
      complete: (res) => {
        setHeaders(res.meta.fields ?? [])
        setPreview((res.data as Record<string, string>[]).slice(0, 5))
      },
    })
  }

  async function handleConfirm() {
    if (!file) return
    const res = await onConfirm(file)
    setResult(res)
  }

  const handleBackdrop = (e: React.MouseEvent<HTMLDialogElement>) => {
    if (e.target === dialogRef.current && !isLoading) onClose()
  }

  return (
    <dialog
      ref={dialogRef}
      onClick={handleBackdrop}
      aria-modal="true"
      className="w-full max-w-2xl rounded-2xl border border-gray-200 bg-white p-0 shadow-xl backdrop:bg-black/40 open:flex open:flex-col"
    >
      <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
        <h2 className="text-base font-semibold text-gray-900">{title}</h2>
        <button
          type="button"
          onClick={onClose}
          disabled={isLoading}
          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100"
        >
          <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div className="flex flex-col gap-4 overflow-y-auto p-6">
        <p className="text-sm text-gray-500">{instructions}</p>

        <label className="flex cursor-pointer flex-col items-center gap-2 rounded-xl border-2 border-dashed border-gray-200 p-6 text-center hover:border-blue-400 hover:bg-blue-50/30 transition-colors">
          <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
          </svg>
          <span className="text-sm text-gray-500">
            {file ? file.name : 'Clique para selecionar arquivo CSV ou Excel'}
          </span>
          <input type="file" accept=".csv,.xlsx,.xls" className="sr-only" onChange={handleFile} />
        </label>

        {preview.length > 0 && (
          <div className="overflow-x-auto rounded-xl border border-gray-200">
            <table className="w-full text-xs">
              <thead>
                <tr className="bg-gray-50">
                  {headers.map(h => (
                    <th key={h} className="px-3 py-2 text-left font-medium text-gray-600">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {preview.map((row, i) => (
                  <tr key={i}>
                    {headers.map(h => (
                      <td key={h} className="px-3 py-2 text-gray-700">{row[h] ?? '—'}</td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="px-3 py-2 text-xs text-gray-400 border-t border-gray-100">
              Mostrando {preview.length} linha(s) de preview
            </p>
          </div>
        )}

        {result && (
          <div className={`rounded-xl border p-4 ${result.erros.length === 0 ? 'border-green-200 bg-green-50' : 'border-yellow-200 bg-yellow-50'}`}>
            <p className="text-sm font-medium text-gray-800">
              {result.importados} importado(s){result.erros.length > 0 ? `, ${result.erros.length} erro(s)` : ''}
            </p>
            {result.erros.length > 0 && (
              <ul className="mt-2 flex flex-col gap-1">
                {result.erros.map((e, i) => (
                  <li key={i} className="text-xs text-red-700">
                    Linha {e.linha}: {e.mensagem}
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </div>

      <div className="flex justify-end gap-3 border-t border-gray-100 px-6 py-4">
        <Button variant="secondary" onClick={onClose} disabled={isLoading}>Cancelar</Button>
        <Button onClick={handleConfirm} loading={isLoading} disabled={!file || isLoading || !!result}>
          Confirmar importação
        </Button>
      </div>
    </dialog>
  )
}
