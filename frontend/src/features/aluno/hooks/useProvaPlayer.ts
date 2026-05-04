import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toastEmitter } from '@/lib/toastEmitter'
import { alunoService } from '../services/aluno.service'
import type { RespostasMap, SessaoProvaData } from '../types'

export function useProvaPlayer(provaId: number) {
  const navigate = useNavigate()

  const [sessao, setSessao] = useState<SessaoProvaData | null>(null)
  const [respostas, setRespostas] = useState<RespostasMap>({})
  const [indiceAtual, setIndiceAtual] = useState(0)
  const [isIniciando, setIsIniciando] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isOffline, setIsOffline] = useState(!navigator.onLine)
  const [horaAbaInativa, setHoraAbaInativa] = useState<string | null>(null)

  const respostasRef = useRef(respostas)
  const sessaoRef = useRef(sessao)
  useEffect(() => { respostasRef.current = respostas }, [respostas])
  useEffect(() => { sessaoRef.current = sessao }, [sessao])

  // Iniciar sessão
  const iniciarSessao = useCallback(async () => {
    setIsIniciando(true)
    try {
      const s = await alunoService.iniciarProva(provaId)
      setSessao(s)
      if (s.respostasParciais) {
        setRespostas(s.respostasParciais)
      }
    } catch {
      toastEmitter.emit('error', 'Erro ao iniciar prova. Tente novamente.')
    } finally {
      setIsIniciando(false)
    }
  }, [provaId])

  // Autosave a cada 60s
  useEffect(() => {
    if (!sessao) return
    const id = setInterval(async () => {
      if (Object.keys(respostasRef.current).length === 0) return
      try {
        await alunoService.autosaveProva(provaId, sessao.sessaoId, { respostas: respostasRef.current })
      } catch { /* silencioso */ }
    }, 60_000)
    return () => clearInterval(id)
  }, [provaId, sessao])

  // Backup offline em localStorage
  useEffect(() => {
    if (!sessao) return
    const chave = `prova_backup_${sessao.sessaoId}`

    function handleOffline() {
      setIsOffline(true)
      localStorage.setItem(chave, JSON.stringify(respostasRef.current))
    }

    async function handleOnline() {
      setIsOffline(false)
      const backup = localStorage.getItem(chave)
      if (!backup || !sessaoRef.current) return
      try {
        const r: RespostasMap = JSON.parse(backup)
        await alunoService.autosaveProva(provaId, sessaoRef.current.sessaoId, { respostas: r })
        localStorage.removeItem(chave)
      } catch { /* tenta novamente na próxima janela de autosave */ }
    }

    window.addEventListener('offline', handleOffline)
    window.addEventListener('online', handleOnline)
    return () => {
      window.removeEventListener('offline', handleOffline)
      window.removeEventListener('online', handleOnline)
    }
  }, [provaId, sessao])

  // Detecção de saída de aba (visibilitychange)
  useEffect(() => {
    if (!sessao) return

    function handleVisibility() {
      const s = sessaoRef.current
      if (!s) return

      if (document.visibilityState === 'hidden') {
        alunoService.autosaveProva(provaId, s.sessaoId, {
          respostas: respostasRef.current,
          eventoVisibilidade: 'hidden',
        }).catch(() => { /* silencioso */ })
      } else {
        const hora = new Date().toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })
        setHoraAbaInativa(hora)
      }
    }

    document.addEventListener('visibilitychange', handleVisibility)
    return () => document.removeEventListener('visibilitychange', handleVisibility)
  }, [provaId, sessao])

  const setResposta = useCallback((questaoId: number, valor: RespostasMap[number]) => {
    setRespostas(prev => ({ ...prev, [questaoId]: valor }))
  }, [])

  function contarRespondidas(total: number): number {
    return Object.keys(respostas).filter(k => {
      const r = respostas[Number(k)]
      if (r === undefined || r === null) return false
      if (typeof r === 'string') return r.trim().length > 0
      if (typeof r === 'number') return true
      if (Array.isArray(r)) return r.length > 0
      return false
    }).length
  }

  async function entregar() {
    if (!sessao) return
    setIsSubmitting(true)
    try {
      await alunoService.entregarProva(provaId, sessao.sessaoId, respostas)
      localStorage.removeItem(`prova_backup_${sessao.sessaoId}`)
      toastEmitter.emit('success', 'Prova entregue com sucesso!')
      navigate(`/aluno/provas/${provaId}/resultado`)
    } catch {
      toastEmitter.emit('error', 'Erro ao entregar prova. Tente novamente.')
    } finally {
      setIsSubmitting(false)
    }
  }

  async function onTimerExpire() {
    toastEmitter.emit('error', 'Tempo esgotado! Entregando automaticamente...')
    await entregar()
  }

  return {
    sessao,
    indiceAtual,
    setIndiceAtual,
    respostas,
    setResposta,
    isIniciando,
    isSubmitting,
    isOffline,
    horaAbaInativa,
    fecharModalAba: () => setHoraAbaInativa(null),
    iniciarSessao,
    contarRespondidas,
    entregar,
    onTimerExpire,
  }
}
