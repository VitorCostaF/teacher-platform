import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { toastEmitter } from '@/lib/toastEmitter'
import { adminService } from '../services/admin.service'
import { ProfessoresTable } from '../components/ProfessoresTable'
import { ConvidarProfessorModal } from '../components/ConvidarProfessorModal'
import { ImportarCSVModal } from '../components/ImportarCSVModal'

export function ProfessoresPage() {
  const [showConvidar, setShowConvidar] = useState(false)
  const [showImportar, setShowImportar] = useState(false)

  const { mutateAsync: importar, isPending: importando } = useMutation({
    mutationFn: (file: File) => adminService.importarProfessores(file).then(r => r.data),
    onSuccess: (res) => {
      toastEmitter.emit(
        res.erros?.length ? 'warning' : 'success',
        `${res.importados} professor(es) importado(s)${res.erros?.length ? `, ${res.erros.length} erro(s)` : ''}.`,
      )
    },
  })

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Professores</h1>
            <p className="text-sm text-gray-500">Gerencie convites e status dos professores</p>
          </div>
          <div className="flex gap-3">
            <Button variant="secondary" onClick={() => setShowImportar(true)}>
              Importar CSV
            </Button>
            <Button onClick={() => setShowConvidar(true)}>
              + Convidar Professor
            </Button>
          </div>
        </div>

        <ProfessoresTable
          onConvidar={() => setShowConvidar(true)}
          onImportar={() => setShowImportar(true)}
        />

        <ConvidarProfessorModal
          isOpen={showConvidar}
          onClose={() => setShowConvidar(false)}
        />

        <ImportarCSVModal
          isOpen={showImportar}
          title="Importar Professores via CSV"
          instructions="O arquivo deve ter colunas: nome, email. Utilize vírgula como separador."
          isLoading={importando}
          onClose={() => setShowImportar(false)}
          onConfirm={importar}
        />
      </div>
    </div>
  )
}
