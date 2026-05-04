import type { QuestaoAtividade, RespostasMap } from '../types'
import { QuestaoMultiplaEscolha } from './questoes/QuestaoMultiplaEscolha'
import { QuestaoVerdadeiroFalso } from './questoes/QuestaoVerdadeiroFalso'
import { QuestaoDissertativa } from './questoes/QuestaoDissertativa'
import { QuestaoUploadArquivo } from './questoes/QuestaoUploadArquivo'

interface Props {
  questao: QuestaoAtividade
  respostas: RespostasMap
  onResposta: (questaoId: number, valor: RespostasMap[number]) => void
}

export function QuestaoRenderer({ questao, respostas, onResposta }: Props) {
  const resposta = respostas[questao.id]

  switch (questao.tipo) {
    case 'MULTIPLA_ESCOLHA':
      return (
        <QuestaoMultiplaEscolha
          questao={questao}
          resposta={typeof resposta === 'number' ? resposta : null}
          onChange={v => onResposta(questao.id, v)}
        />
      )
    case 'VERDADEIRO_FALSO':
      return (
        <QuestaoVerdadeiroFalso
          resposta={typeof resposta === 'number' ? resposta : null}
          onChange={v => onResposta(questao.id, v)}
        />
      )
    case 'DISSERTATIVA':
    case 'LEITURA_COM_PERGUNTAS':
    case 'PESQUISA_COM_ROTEIRO':
    case 'PROJETO_COM_ETAPAS':
      return (
        <QuestaoDissertativa
          resposta={typeof resposta === 'string' ? resposta : ''}
          onChange={v => onResposta(questao.id, v)}
        />
      )
    case 'UPLOAD':
      return (
        <QuestaoUploadArquivo
          urlAtual={typeof resposta === 'string' ? resposta : ''}
          onChange={v => onResposta(questao.id, v)}
        />
      )
    default:
      return (
        <p className="text-sm text-gray-400 italic">Tipo de questão não suportado: {questao.tipo}</p>
      )
  }
}
