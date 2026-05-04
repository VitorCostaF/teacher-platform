import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Button } from '@/components/ui/Button'
import { toastEmitter } from '@/lib/toastEmitter'
import { adminService } from '../services/admin.service'
import { AlunosTable } from '../components/AlunosTable'
import { NovoAlunoModal } from '../components/NovoAlunoModal'
import { ImportarCSVModal } from '../components/ImportarCSVModal'

export function AlunosAdminPage() {
  const [showNovo, setShowNovo] = useState(false)
  const [showImportar, setShowImportar] = useState(false)

  const { mutateAsync: importar, isPending: importando } = useMutation({
    mutationFn: (file: File) => adminService.importarAlunos(file).then(r => r.data),
    onSuccess: (res) => {
      toastEmitter.emit(
        res.erros?.length ? 'warning' : 'success',
        `${res.importados} aluno(s) importado(s)${res.erros?.length ? `, ${res.erros.length} erro(s)` : ''}.`,
      )
    },
  })

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="mx-auto max-w-7xl px-4 py-8 sm:px-6 lg:px-8">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Alunos</h1>
            <p className="text-sm text-gray-500">Gerencie matrículas e transferências</p>
          </div>
          <div className="flex gap-3">
            <Button variant="secondary" onClick={() => setShowImportar(true)}>
              Importar CSV
            </Button>
            <Button onClick={() => setShowNovo(true)}>
              + Novo Aluno
            </Button>
          </div>
        </div>

        <AlunosTable />

        <NovoAlunoModal
          isOpen={showNovo}
          onClose={() => setShowNovo(false)}
        />

        <ImportarCSVModal
          isOpen={showImportar}
          title="Importar Alunos via CSV"
          instructions="O arquivo deve ter colunas: nome, email, turma_id. Utilize vírgula como separador."
          isLoading={importando}
          onClose={() => setShowImportar(false)}
          onConfirm={importar}
        />
      </div>
    </div>
  )
}
