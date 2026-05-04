import { useRef, useState } from 'react'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import * as XLSX from 'xlsx'
import { toastEmitter } from '@/lib/toastEmitter'
import type { AulaGrade } from '../types'

interface Props {
  tableRef: React.RefObject<HTMLDivElement | null>
  aulas: AulaGrade[]
  onSalvar: () => Promise<void>
  isSalvando: boolean
}

export function GradeExportButtons({ tableRef, aulas, onSalvar, isSalvando }: Props) {
  const [exporting, setExporting] = useState<'pdf' | 'xlsx' | null>(null)

  async function handleExportPDF() {
    if (!tableRef.current) return
    setExporting('pdf')
    try {
      const canvas = await html2canvas(tableRef.current, { scale: 2, useCORS: true })
      const imgData = canvas.toDataURL('image/png')
      const pdf = new jsPDF({ orientation: 'landscape', unit: 'pt', format: 'a4' })
      const pageWidth = pdf.internal.pageSize.getWidth()
      const imgWidth = pageWidth - 40
      const imgHeight = (canvas.height * imgWidth) / canvas.width
      pdf.addImage(imgData, 'PNG', 20, 20, imgWidth, imgHeight)
      pdf.save('grade-de-aulas.pdf')
      toastEmitter.emit('success', 'PDF exportado com sucesso!')
    } catch {
      toastEmitter.emit('error', 'Erro ao exportar PDF.')
    } finally {
      setExporting(null)
    }
  }

  function handleExportXLSX() {
    setExporting('xlsx')
    try {
      const dados = aulas.map(a => ({
        Semana: a.semana,
        Aula: a.aula,
        Conteúdo: a.conteudo,
        Objetivos: a.objetivos,
        'Recursos Sugeridos': a.recursosSugeridos,
      }))
      const ws = XLSX.utils.json_to_sheet(dados)
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, 'Grade de Aulas')
      XLSX.writeFile(wb, 'grade-de-aulas.xlsx')
      toastEmitter.emit('success', 'Planilha exportada com sucesso!')
    } catch {
      toastEmitter.emit('error', 'Erro ao exportar planilha.')
    } finally {
      setExporting(null)
    }
  }

  const btnBase = 'flex items-center gap-2 rounded-lg border px-4 py-2 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-50'

  return (
    <div className="flex flex-wrap gap-3">
      <button
        type="button"
        onClick={handleExportPDF}
        disabled={exporting !== null}
        className={`${btnBase} border-red-200 bg-red-50 text-red-700 hover:bg-red-100`}
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        {exporting === 'pdf' ? 'Exportando...' : 'Exportar PDF'}
      </button>

      <button
        type="button"
        onClick={handleExportXLSX}
        disabled={exporting !== null}
        className={`${btnBase} border-green-200 bg-green-50 text-green-700 hover:bg-green-100`}
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        {exporting === 'xlsx' ? 'Exportando...' : 'Exportar XLSX'}
      </button>

      <button
        type="button"
        onClick={onSalvar}
        disabled={isSalvando}
        className={`${btnBase} border-blue-600 bg-blue-600 text-white hover:bg-blue-700`}
      >
        <svg className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4" />
        </svg>
        {isSalvando ? 'Salvando...' : 'Salvar na plataforma'}
      </button>
    </div>
  )
}
