import { useCallback, useState } from 'react'
import { toastEmitter } from '@/lib/toastEmitter'
import { iaService } from '../services/ia.service'
import type { ProvaConfig, QuestaoGerada, TipoQuestao } from '../types'

function buildConfigInicial(topicos: string[]): ProvaConfig {
  return {
    turmaId: null,
    disciplina: '',
    serie: '',
    titulo: '',
    dificuldade: 'MEDIO',
    duracaoMinutos: '',
    quantidades: { MULTIPLA_ESCOLHA: 5 },
    fonte: topicos.length > 0 ? 'topicos' : 'texto',
    conteudoTexto: '',
    topicos,
  }
}

function gerarIdLocal() {
  return `local_${Date.now()}_${Math.random().toString(36).slice(2)}`
}

function validarConfig(config: ProvaConfig): string | null {
  if (!config.turmaId) return 'Selecione uma turma'
  if (!config.titulo.trim()) return 'Informe o título da prova'
  const total = Object.values(config.quantidades).reduce((s, n) => s + (n ?? 0), 0)
  if (total === 0) return 'Selecione ao menos um tipo de questão'
  if (config.fonte === 'texto' && !config.conteudoTexto.trim() && config.topicos.length === 0)
    return 'Informe o conteúdo ou tópicos para gerar a prova'
  return null
}

export function useGeradorProvas(topicosIniciais: string[] = []) {
  const [config, setConfig] = useState<ProvaConfig>(() => buildConfigInicial(topicosIniciais))
  const [questoes, setQuestoes] = useState<QuestaoGerada[]>([])
  const [isGenerating, setIsGenerating] = useState(false)
  const [isUploading, setIsUploading] = useState(false)
  const [avisoConteudo, setAvisoConteudo] = useState<string | null>(null)

  const atualizarConfig = useCallback(<K extends keyof ProvaConfig>(campo: K, valor: ProvaConfig[K]) => {
    setConfig(prev => ({ ...prev, [campo]: valor }))
  }, [])

  const gerarComIA = useCallback(async () => {
    const erro = validarConfig(config)
    if (erro) {
      toastEmitter.emit('error', erro)
      return
    }

    setIsGenerating(true)
    try {
      const res = await iaService.gerarProva({
        disciplina: config.disciplina,
        serie: config.serie,
        dificuldade: config.dificuldade,
        quantidadesPorTipo: config.quantidades,
        conteudoTexto: config.conteudoTexto || undefined,
        topicos: config.topicos.length ? config.topicos : undefined,
      })

      const comIds = res.questoes.map(q => ({ ...q, id: q.id || gerarIdLocal() }))
      setQuestoes(comIds)

      if (comIds.length === 0) {
        toastEmitter.emit('error', 'A IA não gerou questões. Tente com mais conteúdo.')
      }
    } catch {
      toastEmitter.emit('error', 'Erro ao gerar prova. Tente novamente.')
    } finally {
      setIsGenerating(false)
    }
  }, [config])

  const regenerarQuestao = useCallback(async (id: string) => {
    const questao = questoes.find(q => q.id === id)
    if (!questao) return

    setQuestoes(prev =>
      prev.map(q => q.id === id ? { ...q, _regenerando: true } as QuestaoGerada & { _regenerando?: boolean } : q)
    )

    try {
      const res = await iaService.regenerarQuestao({
        disciplina: config.disciplina,
        serie: config.serie,
        tipo: questao.tipo,
        dificuldade: questao.dificuldade,
        topico: questao.topico,
      })
      const nova = res.questoes[0]
      if (nova) {
        setQuestoes(prev => prev.map(q => q.id === id ? { ...nova, id } : q))
      }
    } catch {
      toastEmitter.emit('error', 'Erro ao regenerar questão.')
      setQuestoes(prev => prev.map(q => {
        const q2 = q as QuestaoGerada & { _regenerando?: boolean }
        if (q2.id === id) { delete q2._regenerando }
        return q2
      }))
    }
  }, [questoes, config.disciplina, config.serie])

  const editarQuestao = useCallback((id: string, changes: Partial<QuestaoGerada>) => {
    setQuestoes(prev => prev.map(q => q.id === id ? { ...q, ...changes } : q))
  }, [])

  const removerQuestao = useCallback((id: string) => {
    setQuestoes(prev => prev.filter(q => q.id !== id))
  }, [])

  const adicionarQuestaoManual = useCallback(() => {
    const nova: QuestaoGerada = {
      id: gerarIdLocal(),
      tipo: 'MULTIPLA_ESCOLHA',
      enunciado: '',
      alternativas: ['', '', '', ''],
      gabarito: 0,
      dificuldade: config.dificuldade === 'MISTO' ? 'MEDIO' : config.dificuldade,
      topico: '',
    }
    setQuestoes(prev => [...prev, nova])
  }, [config.dificuldade])

  const uploadArquivo = useCallback(async (file: File) => {
    setIsUploading(true)
    setAvisoConteudo(null)
    try {
      const res = await iaService.uploadConteudo(file)
      setConfig(prev => ({ ...prev, conteudoTexto: res.texto, fonte: 'texto' }))
      if (res.aviso) setAvisoConteudo(res.aviso)
    } catch {
      toastEmitter.emit('error', 'Erro ao fazer upload do arquivo.')
    } finally {
      setIsUploading(false)
    }
  }, [])

  const podeGerar = !isGenerating && !!config.turmaId && !!config.titulo.trim() &&
    Object.values(config.quantidades).some(n => (n ?? 0) > 0)

  return {
    config,
    questoes,
    isGenerating,
    isUploading,
    avisoConteudo,
    podeGerar,
    atualizarConfig,
    gerarComIA,
    regenerarQuestao,
    editarQuestao,
    removerQuestao,
    adicionarQuestaoManual,
    uploadArquivo,
  }
}
