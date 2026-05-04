import type { ProvaCalendario } from '../types'

interface CalendarioProvasProps {
  provas: ProvaCalendario[]
}

function formatData(iso: string) {
  return new Date(iso + 'T00:00:00').toLocaleDateString('pt-BR', {
    weekday: 'short',
    day: '2-digit',
    month: '2-digit',
  })
}

export function CalendarioProvas({ provas }: CalendarioProvasProps) {
  const hoje = new Date().toISOString().split('T')[0]
  const futuras = provas.filter(p => p.data >= hoje)
  const passadas = provas.filter(p => p.data < hoje)

  return (
    <div className="flex flex-col gap-4">
      {futuras.length > 0 && (
        <section className="flex flex-col gap-2">
          <h3 className="text-sm font-semibold text-gray-700">Próximas provas</h3>
          <div className="flex flex-col gap-2">
            {futuras.map(prova => (
              <div
                key={prova.id}
                className="flex items-center gap-3 rounded-xl border border-blue-100 bg-blue-50 px-4 py-3"
              >
                <div className="flex flex-col gap-0.5">
                  <p className="text-sm font-semibold text-gray-900">{prova.titulo}</p>
                  <p className="text-xs text-gray-500">
                    {prova.disciplina} · {formatData(prova.data)}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </section>
      )}

      {passadas.length > 0 && (
        <section className="flex flex-col gap-2">
          <h3 className="text-sm font-semibold text-gray-700">Histórico</h3>
          <div className="flex flex-col gap-2">
            {passadas.map(prova => (
              <div
                key={prova.id}
                className="flex items-center justify-between gap-3 rounded-xl border border-gray-200 bg-white px-4 py-3"
              >
                <div className="flex flex-col gap-0.5">
                  <p className="text-sm font-medium text-gray-800">{prova.titulo}</p>
                  <p className="text-xs text-gray-500">
                    {prova.disciplina} · {formatData(prova.data)}
                  </p>
                </div>
                {prova.nota !== undefined && prova.nota !== null && (
                  <span
                    className={`shrink-0 text-sm font-bold ${prova.nota >= 5 ? 'text-green-700' : 'text-red-700'}`}
                  >
                    {prova.nota.toFixed(1)}
                  </span>
                )}
              </div>
            ))}
          </div>
        </section>
      )}

      {provas.length === 0 && (
        <p className="text-center text-sm text-gray-500">Nenhuma prova registrada.</p>
      )}
    </div>
  )
}
