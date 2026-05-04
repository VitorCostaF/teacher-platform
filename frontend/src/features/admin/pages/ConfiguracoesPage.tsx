import { ConfiguracoesForm } from '../components/ConfiguracoesForm'

export function ConfiguracoesPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-2xl px-4 py-8 sm:px-6">
        <div className="mb-6">
          <h1 className="text-2xl font-bold text-gray-900">Configurações</h1>
          <p className="text-sm text-gray-500">Gerencie as configurações pedagógicas da escola</p>
        </div>
        <ConfiguracoesForm />
      </div>
    </div>
  )
}
