import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toastEmitter } from '@/lib/toastEmitter'
import type { AlunoTurma } from '@/features/turmas/types'
import { frequenciaService } from '../services/frequencia.service'
import type { FrequenciaAluno, LancarFrequenciaDto, StatusFrequencia } from '../types'

function formatDate(date: Date): string {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function buildEmptyMap(alunos: AlunoTurma[]): Record<string, FrequenciaAluno> {
  const map: Record<string, FrequenciaAluno> = {}
  alunos.forEach(a => { map[a.id] = { alunoId: a.id, status: null, observacao: '' } })
  return map
}

export function useFrequencia(turmaId: number, alunos: AlunoTurma[]) {
  const navigate = useNavigate()
  const [selectedDate, setSelectedDate] = useState<Date>(new Date())
  const [frequenciaMap, setFrequenciaMap] = useState<Record<string, FrequenciaAluno>>({})
  const [existingId, setExistingId] = useState<number | null>(null)
  const [isLoadingDate, setIsLoadingDate] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [isDirty, setIsDirty] = useState(false)

  const alunosRef = useRef(alunos)
  alunosRef.current = alunos

  const loadForDate = useCallback(async (date: Date) => {
    const dateStr = formatDate(date)
    setIsLoadingDate(true)
    try {
      const data = await frequenciaService.buscarPorData(turmaId, dateStr)
      const map = buildEmptyMap(alunosRef.current)
      if (data?.alunos) {
        setExistingId(data.id)
        data.alunos.forEach((a: FrequenciaAluno) => {
          if (map[a.alunoId]) map[a.alunoId] = { ...a, observacao: a.observacao ?? '' }
        })
      } else {
        setExistingId(null)
      }
      setFrequenciaMap(map)
      setIsDirty(false)
    } finally {
      setIsLoadingDate(false)
    }
  }, [turmaId])

  // Load when alunos first populate
  useEffect(() => {
    if (alunos.length > 0) {
      loadForDate(selectedDate)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [alunos.length > 0])

  const mudarData = useCallback((novaData: Date) => {
    setSelectedDate(novaData)
    loadForDate(novaData)
  }, [loadForDate])

  const setStatus = useCallback((alunoId: string, status: StatusFrequencia) => {
    setFrequenciaMap(prev => ({
      ...prev,
      [alunoId]: { ...prev[alunoId], status },
    }))
    setIsDirty(true)
  }, [])

  const setObservacao = useCallback((alunoId: string, observacao: string) => {
    setFrequenciaMap(prev => ({
      ...prev,
      [alunoId]: { ...prev[alunoId], observacao },
    }))
    setIsDirty(true)
  }, [])

  const marcarTodosPresentes = useCallback(() => {
    setFrequenciaMap(prev => {
      const next = { ...prev }
      Object.keys(next).forEach(id => {
        next[id] = { ...next[id], status: 'PRESENTE' }
      })
      return next
    })
    setIsDirty(true)
  }, [])

  const totalPreenchidos = Object.values(frequenciaMap).filter(a => a.status !== null).length
  const isTodoPreenchido = totalPreenchidos === alunos.length && alunos.length > 0

  const salvar = useCallback(async () => {
    const payload: LancarFrequenciaDto = {
      data: formatDate(selectedDate),
      alunos: Object.values(frequenciaMap).filter(a => a.status !== null) as FrequenciaAluno[],
    }
    setIsSaving(true)
    try {
      if (existingId) {
        await frequenciaService.editar(turmaId, existingId, payload)
      } else {
        await frequenciaService.lancar(turmaId, payload)
      }
      toastEmitter.emit('success', 'Frequência salva com sucesso!')
      navigate(`/professor/turmas/${turmaId}`)
    } catch (e) {
      toastEmitter.emit('error', e instanceof Error ? e.message : 'Erro ao salvar frequência')
    } finally {
      setIsSaving(false)
    }
  }, [selectedDate, frequenciaMap, existingId, turmaId, navigate])

  return {
    selectedDate,
    frequenciaMap,
    existingId,
    isLoadingDate,
    isSaving,
    isDirty,
    totalPreenchidos,
    isTodoPreenchido,
    mudarData,
    setStatus,
    setObservacao,
    marcarTodosPresentes,
    salvar,
  }
}
