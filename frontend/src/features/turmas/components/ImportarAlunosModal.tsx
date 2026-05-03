import { useRef, useState } from 'react'
import Papa from 'papaparse'
import * as XLSX from 'xlsx'
import { Button } from '@/components/ui/Button'
import type { ImportacaoResult } from '../types'
import { turmasService } from '../services/turmas.service'

interface ImportarAlunosModalProps {
  isOpen: boolean
  turmaId: number
  onClose: () => void
  onImportado: () => void
}

interface PreviewRow {
  linha: number
  nome: string
  email: string
  erroLocal?: string
  erroServidor?: string
}

function parseFile(file: File): Promise<Array<Record<string, string>>> {
  return new Promise((resolve, reject) => {
    const isXlsx =
      file.name.toLowerCase().endsWith('.xlsx') ||
      file.type.includes('spreadsheetml')

    if (isXlsx) {
      const reader = new FileReader()
      reader.onload = e => {
        try {
          const data = new Uint8Array(e.target!.result as ArrayBuffer)
          const wb = XLSX.read(data, { type: 'array' })
          const ws = wb.Sheets[wb.SheetNames[0]]
          const rows = XLSX.utils.sheet_to_json<Record<string, string>>(ws, { defval: '' })
          resolve(rows)
        } catch (err) {
          reject(err)
        }
      }
      reader.onerror = () => reject(new Error('Erro ao ler arquivo'))
      reader.readAsArrayBuffer(file)
    } else {
      Papa.parse<Record<string, string>>(file, {
        header: true,
        skipEmptyLines: true,
        complete: result => resolve(result.data),
        error: err => reject(new Error(err.message)),
      })
    }
  })
}

function buildPreview(rows: Array<Record<string, string>>): PreviewRow[] {
  return rows.map((row, i) => {
    const email = (row['email'] ?? row['Email'] ?? row['E-mail'] ?? '').trim()
    const nome = (row['nome'] ?? row['Nome'] ?? '').trim()
    return {
      linha: i + 2,
      nome,
      email,
      erroLocal: !email ? "Campo 'email' ausente ou vazio" : undefined,
    }
  })
}

export function ImportarAlunosModal({ isOpen, turmaId, onClose, onImportado }: ImportarAlunosModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)
  const [file, setFile] = useState<File | null>(null)
  const [preview, setPreview] = useState<PreviewRow[]>([])
  const [isParsing, setIsParsing] = useState(false)
  const [isImporting, setIsImporting] = useState(false)
  const [isDragging, setIsDragging] = useState(false)
  const [importResult, setImportResult] = useState<ImportacaoResult | null>(null)
  const [erroGlobal, setErroGlobal] = useState<string | null>(null)

  const handleFile = async (f: File) => {
    setFile(f)
    setPreview([])
    setImportResult(null)
    setErroGlobal(null)
    setIsParsing(true)
    try {
      const rows = await parseFile(f)
      setPreview(buildPreview(rows))
    } catch {
      setErroGlobal('Não foi possível ler o arquivo. Verifique o formato.')
    } finally {
      setIsParsing(false)
    }
  }

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault()
    setIsDragging(false)
    const f = e.dataTransfer.files[0]
    if (f) handleFile(f)
  }

  const handleImportar = async () => {
    if (!file) return
    setIsImporting(true)
    setErroGlobal(null)
    try {
      const result = await turmasService.importarAlunos(turmaId, file)
      setImportResult(result)
      if (result.erros.length === 0) {
        onImportado()
        onClose()
        return
      }
      // Anota erros do servidor no preview
      setPreview(prev =>
        prev.map(row => {
          const erroSrv = result.erros.find(e => e.linha === row.linha)
          return erroSrv ? { ...row, erroServidor: erroSrv.motivo } : row
        })
      )
    } catch (e) {
      setErroGlobal(e instanceof Error ? e.message : 'Erro ao importar')
    } finally {
      setIsImporting(false)
    }
  }

  const resetState = () => {
    setFile(null)
    setPreview([])
    setImportResult(null)
    setErroGlobal(null)
    setIsDragging(false)
  }

  if (!isOpen) return null

  const validRows = preview.filter(r => !r.erroLocal && !r.erroServidor).length
  const erroRows = preview.filter(r => r.erroLocal || r.erroServidor)
  const canImport = file !== null && !isParsing && preview.length > 0 && preview.some(r => !r.erroLocal)

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="flex w-full max-w-2xl flex-col rounded-2xl border border-gray-200 bg-white shadow-xl">
        <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h2 className="text-base font-semibold text-gray-900">Importar alunos via planilha</h2>
          <button
            onClick={() => { resetState(); onClose() }}
            className="rounded-lg p-1 text-gray-400 hover:text-gray-600 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-gray-400"
            aria-label="Fechar"
          >
            <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>

        <div className="flex flex-col gap-4 overflow-y-auto p-6">
          {/* Drop zone */}
          <div
            onDragOver={e => { e.preventDefault(); setIsDragging(true) }}
            onDragLeave={() => setIsDragging(false)}
            onDrop={handleDrop}
            onClick={() => inputRef.current?.click()}
            className={[
              'flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed px-6 py-8 transition-colors',
              isDragging ? 'border-blue-400 bg-blue-50' : 'border-gray-300 hover:border-blue-400 hover:bg-gray-50',
            ].join(' ')}
          >
            <svg className="h-8 w-8 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" /></svg>
            <p className="text-sm text-gray-600">
              {file ? file.name : 'Arraste um arquivo ou clique para selecionar'}
            </p>
            <p className="text-xs text-gray-400">CSV ou XLSX — coluna obrigatória: email</p>
            <input
              ref={inputRef}
              type="file"
              accept=".csv,.xlsx"
              className="hidden"
              onChange={e => { const f = e.target.files?.[0]; if (f) handleFile(f) }}
            />
          </div>

          {/* Preview */}
          {isParsing && (
            <p className="text-center text-sm text-gray-500">Lendo arquivo...</p>
          )}

          {preview.length > 0 && !isParsing && (
            <>
              <div className="flex items-center justify-between text-sm">
                <span className="text-gray-600">
                  {preview.length} linha{preview.length !== 1 ? 's' : ''} encontrada{preview.length !== 1 ? 's' : ''}
                  {importResult && ` — ${importResult.importados} importado${importResult.importados !== 1 ? 's' : ''}`}
                </span>
                {erroRows.length > 0 && (
                  <span className="font-medium text-red-600">{erroRows.length} com erro</span>
                )}
              </div>

              <div className="max-h-64 overflow-y-auto rounded-lg border border-gray-200">
                <table className="w-full text-sm">
                  <thead className="bg-gray-50 text-xs uppercase text-gray-500">
                    <tr>
                      <th className="px-3 py-2 text-left">#</th>
                      <th className="px-3 py-2 text-left">Nome</th>
                      <th className="px-3 py-2 text-left">E-mail</th>
                      <th className="px-3 py-2 text-left">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {preview.map(row => {
                      const hasError = row.erroLocal || row.erroServidor
                      return (
                        <tr key={row.linha} className={hasError ? 'bg-red-50' : ''}>
                          <td className="px-3 py-2 text-gray-400">{row.linha}</td>
                          <td className="px-3 py-2 text-gray-700">{row.nome || '—'}</td>
                          <td className="px-3 py-2 text-gray-700">{row.email || '—'}</td>
                          <td className="px-3 py-2">
                            {hasError ? (
                              <span className="text-xs text-red-600">{row.erroServidor ?? row.erroLocal}</span>
                            ) : importResult ? (
                              <span className="text-xs text-green-600">Importado</span>
                            ) : null}
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            </>
          )}

          {erroGlobal && (
            <p className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700">{erroGlobal}</p>
          )}
        </div>

        <div className="flex justify-end gap-3 border-t border-gray-100 px-6 py-4">
          <Button variant="secondary" onClick={() => { resetState(); onClose() }} disabled={isImporting}>
            Cancelar
          </Button>
          <Button onClick={handleImportar} loading={isImporting} disabled={!canImport || isImporting}>
            {importResult ? `Reimportar (${validRows} aluno${validRows !== 1 ? 's' : ''})` : `Confirmar importação`}
          </Button>
        </div>
      </div>
    </div>
  )
}
