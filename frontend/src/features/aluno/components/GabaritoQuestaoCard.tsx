import type { GabaritoQuestao } from '../types'

interface Props {
  questao: GabaritoQuestao
}

function StatusBadge({ correta }: { correta: boolean | undefined }) {
  if (correta === undefined) {
    return (
      <span className="rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-500">
        Aguardando
      </span>
    )
  }
  return correta ? (
    <span className="rounded-full bg-green-100 px-2.5 py-0.5 text-xs font-medium text-green-700">
      ✅ Correta
    </span>
  ) : (
    <span className="rounded-full bg-red-100 px-2.5 py-0.5 text-xs font-medium text-red-700">
      ❌ Incorreta
    </span>
  )
}

function MultiplaEscolha({ questao }: { questao: GabaritoQuestao }) {
  const respostaAluno = typeof questao.respostaAluno === 'number' ? questao.respostaAluno : -1
  const respostaCorreta = typeof questao.respostaCorreta === 'number' ? questao.respostaCorreta : -1

  return (
    <div className="flex flex-col gap-1.5">
      {questao.alternativas?.map((alt, i) => {
        const ehCorreta = i === respostaCorreta
        const ehAluno = i === respostaAluno
        const errada = ehAluno && !ehCorreta

        return (
          <div
            key={i}
            className={[
              'flex items-center gap-2 rounded-lg px-3 py-2 text-sm',
              ehCorreta ? 'bg-green-50 font-medium text-green-800' : '',
              errada ? 'bg-red-50 text-red-700 line-through' : '',
              !ehCorreta && !errada ? 'bg-gray-50 text-gray-600' : '',
            ].filter(Boolean).join(' ')}
          >
            <span className="shrink-0 font-semibold">{String.fromCharCode(65 + i)})</span>
            {alt}
            {ehCorreta && <span className="ml-auto text-green-600">✓</span>}
            {errada && <span className="ml-auto text-red-500">✗</span>}
          </div>
        )
      })}
    </div>
  )
}

function VerdadeiroFalso({ questao }: { questao: GabaritoQuestao }) {
  const respostaAluno = questao.respostaAluno
  const respostaCorreta = questao.respostaCorreta
  const opcoes = ['Falso', 'Verdadeiro']

  return (
    <div className="flex gap-2">
      {opcoes.map((op, i) => {
        const ehCorreta = i === respostaCorreta
        const ehAluno = i === respostaAluno
        const errada = ehAluno && !ehCorreta

        return (
          <span
            key={op}
            className={[
              'rounded-lg px-3 py-1.5 text-sm font-medium',
              ehCorreta ? 'bg-green-50 text-green-800' : '',
              errada ? 'bg-red-50 text-red-700 line-through' : '',
              !ehCorreta && !errada ? 'bg-gray-50 text-gray-500' : '',
            ].filter(Boolean).join(' ')}
          >
            {op}
            {ehCorreta && ' ✓'}
            {errada && ' ✗'}
          </span>
        )
      })}
    </div>
  )
}

function Dissertativa({ questao }: { questao: GabaritoQuestao }) {
  const corrigida = questao.correta !== undefined || questao.observacaoProfessor

  return (
    <div className="flex flex-col gap-3">
      <div className="rounded-lg bg-gray-50 p-3">
        <p className="mb-1 text-xs font-medium text-gray-500">Sua resposta</p>
        <p className="text-sm text-gray-800 leading-relaxed whitespace-pre-wrap">
          {questao.respostaAluno || <em className="text-gray-400">Sem resposta</em>}
        </p>
      </div>

      {corrigida && questao.observacaoProfessor ? (
        <div className="rounded-lg border border-blue-100 bg-blue-50 p-3">
          <p className="mb-1 text-xs font-medium text-blue-600">Observação do professor</p>
          <p className="text-sm text-blue-900 leading-relaxed">{questao.observacaoProfessor}</p>
        </div>
      ) : (
        <p className="text-xs text-gray-400 italic">
          Aguardando correção do professor
        </p>
      )}
    </div>
  )
}

export function GabaritoQuestaoCard({ questao }: Props) {
  const isDissertativa = ['DISSERTATIVA', 'LEITURA_COM_PERGUNTAS', 'PESQUISA_COM_ROTEIRO', 'PROJETO_COM_ETAPAS'].includes(questao.tipo)
  const isVF = questao.tipo === 'VERDADEIRO_FALSO'

  return (
    <div className="flex flex-col gap-3 rounded-xl border border-gray-200 bg-white p-4">
      {/* Header */}
      <div className="flex items-start justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-blue-600 text-xs font-bold text-white">
            {questao.numero}
          </span>
          <p className="text-sm font-medium text-gray-900 leading-snug">{questao.enunciado}</p>
        </div>
        <StatusBadge correta={questao.correta} />
      </div>

      {/* Corpo */}
      {isDissertativa ? (
        <Dissertativa questao={questao} />
      ) : isVF ? (
        <VerdadeiroFalso questao={questao} />
      ) : (
        <MultiplaEscolha questao={questao} />
      )}
    </div>
  )
}
