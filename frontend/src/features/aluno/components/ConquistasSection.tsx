import type { Conquista } from '../types'
import { ConquistaBadge } from './ConquistaBadge'

interface Props {
  conquistas: Conquista[]
}

export function ConquistasSection({ conquistas }: Props) {
  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-sm font-semibold text-gray-700">Conquistas</h2>
      {conquistas.length === 0 ? (
        <div className="flex flex-col items-center gap-2 rounded-xl border border-dashed border-gray-200 py-8 text-center">
          <span className="text-3xl">🏆</span>
          <p className="text-sm text-gray-500">
            Continue estudando para ganhar suas primeiras conquistas!
          </p>
        </div>
      ) : (
        <div className="flex flex-wrap gap-4 rounded-xl border border-gray-200 bg-white p-4">
          {conquistas.map((c, i) => (
            <ConquistaBadge key={i} conquista={c} />
          ))}
        </div>
      )}
    </section>
  )
}
