import { NavLink } from 'react-router-dom'

interface TurmaTabNavigationProps {
  turmaId: number
}

const tabs = [
  { label: 'Alunos', path: '' },
  { label: 'Frequência', path: '/frequencia' },
  { label: 'Atividades', path: '/atividades' },
  { label: 'Desempenho', path: '/desempenho' },
]

export function TurmaTabNavigation({ turmaId }: TurmaTabNavigationProps) {
  const base = `/professor/turmas/${turmaId}`

  return (
    <nav className="border-b border-gray-200" aria-label="Abas da turma">
      <div className="-mb-px flex gap-0">
        {tabs.map(tab => (
          <NavLink
            key={tab.label}
            to={`${base}${tab.path}`}
            end={tab.path === ''}
            className={({ isActive }) =>
              [
                'inline-block border-b-2 px-5 py-3 text-sm font-medium transition-colors',
                isActive
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:border-gray-300 hover:text-gray-700',
              ].join(' ')
            }
          >
            {tab.label}
          </NavLink>
        ))}
      </div>
    </nav>
  )
}
