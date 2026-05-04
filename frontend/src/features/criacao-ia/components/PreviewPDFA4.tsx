import type { ProvaConfig, QuestaoGerada } from '../types'

interface Props {
  config: ProvaConfig
  questoes: QuestaoGerada[]
}

export function PreviewPDFA4({ config, questoes }: Props) {
  function handleAbrirPrevia() {
    const conteudo = buildPrintHTML(config, questoes)
    const janela = window.open('', '_blank')
    if (!janela) return
    janela.document.write(conteudo)
    janela.document.close()
    janela.focus()
    janela.print()
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-gray-700">Prévia de impressão (A4)</p>
        <button
          type="button"
          onClick={handleAbrirPrevia}
          className="flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-3 py-1.5 text-xs font-medium text-gray-700 hover:bg-gray-50"
        >
          <svg className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
              d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
          </svg>
          Abrir prévia em nova aba
        </button>
      </div>

      {/* Container A4 simulado */}
      <div
        className="mx-auto overflow-hidden rounded border border-gray-300 bg-white shadow-sm"
        style={{ width: '210mm', maxWidth: '100%', aspectRatio: '210 / 297' }}
      >
        <div className="overflow-y-auto p-8 text-sm text-gray-900" style={{ height: '100%' }}>
          {/* Cabeçalho */}
          <div className="mb-6 border-b border-gray-300 pb-4 text-center">
            <p className="text-xs text-gray-500">Escola — {config.disciplina}</p>
            <h1 className="mt-1 text-base font-bold">{config.titulo || 'Atividade'}</h1>
            <p className="mt-1 text-xs text-gray-500">
              {config.serie} {config.duracaoMinutos ? `· ${config.duracaoMinutos} min` : ''}
            </p>
            <div className="mt-3 flex justify-between text-xs text-gray-500">
              <span>Nome: ___________________________</span>
              <span>Data: _______________</span>
            </div>
          </div>

          {/* Questões */}
          {questoes.length === 0 ? (
            <p className="text-center text-xs text-gray-400">Gere as questões para ver a prévia</p>
          ) : (
            <ol className="flex flex-col gap-4">
              {questoes.map((q, i) => (
                <li key={q.id} className="flex flex-col gap-1.5">
                  <p className="font-medium">
                    <span className="mr-1">{i + 1}.</span>{q.enunciado}
                  </p>
                  {q.alternativas && q.alternativas.length > 0 && (
                    <ol className="ml-4 flex flex-col gap-0.5" style={{ listStyleType: 'lower-alpha' }}>
                      {q.alternativas.map((alt, ai) => (
                        <li key={ai} className="text-xs text-gray-700">{alt}</li>
                      ))}
                    </ol>
                  )}
                  {q.tipo === 'DISSERTATIVA' && (
                    <div className="mt-1 flex flex-col gap-1">
                      {Array.from({ length: 4 }).map((_, li) => (
                        <div key={li} className="border-b border-gray-300" style={{ height: '1.4rem' }} />
                      ))}
                    </div>
                  )}
                </li>
              ))}
            </ol>
          )}
        </div>
      </div>
    </div>
  )
}

function buildPrintHTML(config: ProvaConfig, questoes: QuestaoGerada[]): string {
  const questoesHTML = questoes.map((q, i) => {
    const alternativasHTML = q.alternativas?.map((alt, ai) =>
      `<li>${String.fromCharCode(97 + ai)}) ${alt}</li>`
    ).join('') ?? ''

    const linhasHTML = q.tipo === 'DISSERTATIVA'
      ? Array.from({ length: 4 }, () => '<div class="linha"></div>').join('')
      : ''

    return `
      <div class="questao">
        <p><strong>${i + 1}.</strong> ${q.enunciado}</p>
        ${alternativasHTML ? `<ol type="a">${alternativasHTML}</ol>` : ''}
        ${linhasHTML}
      </div>
    `
  }).join('')

  return `<!DOCTYPE html>
<html lang="pt-BR">
<head>
<meta charset="UTF-8">
<title>${config.titulo || 'Atividade'}</title>
<style>
  @media print { body { margin: 0; } }
  body { font-family: Arial, sans-serif; font-size: 12pt; color: #111; padding: 20mm; }
  .cabecalho { text-align: center; border-bottom: 1px solid #ccc; padding-bottom: 12px; margin-bottom: 20px; }
  .cabecalho h1 { font-size: 14pt; margin: 4px 0; }
  .cabecalho p { font-size: 10pt; color: #555; margin: 2px 0; }
  .identificacao { display: flex; justify-content: space-between; font-size: 10pt; margin-top: 8px; }
  .questao { margin-bottom: 16px; }
  .questao ol { margin: 6px 0 0 20px; }
  .linha { border-bottom: 1px solid #aaa; height: 20px; margin-top: 6px; }
</style>
</head>
<body>
<div class="cabecalho">
  <p>${config.disciplina}</p>
  <h1>${config.titulo || 'Atividade'}</h1>
  <p>${config.serie}</p>
  <div class="identificacao">
    <span>Nome: _________________________________</span>
    <span>Data: ______________</span>
  </div>
</div>
${questoesHTML}
</body>
</html>`
}
