import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toastEmitter } from '@/lib/toastEmitter'
import { alunoService } from '../services/aluno.service'
import type { AtividadeDetalhe, RespostasMap } from '../types'

export function useAtividadePlayer(atividade: AtividadeDetalhe) {
  const navigate = useNavigate()
  const [indiceAtual, setIndiceAtual] = useState(0)
  const [respostas, setRespostas] = useState<RespostasMap>(atividade.respostasRascunho ?? {})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isAutoSaving, setIsAutoSaving] = useState(false)

  // ref para acessar respostas atuais dentro do intervalo sem stale closure
  const respostasRef = useRef(respostas)
  useEffect(() => { respostasRef.current = respostas }, [respostas])

  // Autosave silencioso a cada 30s
  useEffect(() => {
    const id = setInterval(async () => {
      if (Object.keys(respostasRef.current).length === 0) return
      setIsAutoSaving(true)
      try {
        await alunoService.salvarRascunho(atividade.id, respostasRef.current)
      } catch {
        // falha silenciosa — tenta na próxima janela
      } finally {
        setIsAutoSaving(false)
      }
    }, 30_000)
    return () => clearInterval(id)
  }, [atividade.id])

  const setResposta = useCallback((questaoId: number, valor: RespostasMap[number]) => {
    setRespostas(prev => ({ ...prev, [questaoId]: valor }))
  }, [])

  function contarRespondidas(): number {
    return atividade.questoes.filter(q => {
      const r = respostas[q.id]
      if (r === undefined || r === null) return false
      if (typeof r === 'string') return r.trim().length > 0
      if (typeof r === 'number') return true
      if (Array.isArray(r)) return r.length > 0
      return false
    }).length
  }

  function isTodaRespondida(): boolean {
    return contarRespondidas() === atividade.questoes.length
  }

  async function salvarRascunho() {
    try {
      await alunoService.salvarRascunho(atividade.id, respostas)
      toastEmitter.emit('success', 'Rascunho salvo!')
    } catch {
      toastEmitter.emit('error', 'Erro ao salvar rascunho.')
    }
  }

  async function entregar() {
    setIsSubmitting(true)
    try {
      await alunoService.entregar(atividade.id, respostas)
      toastEmitter.emit('success', 'Atividade entregue com sucesso!')
      navigate(`/aluno/atividades/${atividade.id}/resultado`)
    } catch {
      toastEmitter.emit('error', 'Erro ao entregar atividade. Tente novamente.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return {
    indiceAtual,
    setIndiceAtual,
    respostas,
    setResposta,
    isSubmitting,
    isAutoSaving,
    respondidas: contarRespondidas(),
    isTodaRespondida,
    salvarRascunho,
    entregar,
  }
}
