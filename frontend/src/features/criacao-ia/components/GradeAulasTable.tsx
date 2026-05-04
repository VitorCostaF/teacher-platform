import { useState, useRef } from 'react'
import {
  DndContext,
  closestCenter,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  type DragEndEvent,
} from '@dnd-kit/core'
import {
  SortableContext,
  sortableKeyboardCoordinates,
  useSortable,
  verticalListSortingStrategy,
  arrayMove,
} from '@dnd-kit/sortable'
import { CSS } from '@dnd-kit/utilities'
import type { AulaGrade } from '../types'

interface Props {
  aulas: AulaGrade[]
  onChange: (aulas: AulaGrade[]) => void
}

type CampoEditavel = 'conteudo' | 'objetivos' | 'recursosSugeridos'

interface CellProps {
  value: string
  onSave: (v: string) => void
}

function EditableCell({ value, onSave }: CellProps) {
  const [editing, setEditing] = useState(false)
  const [draft, setDraft] = useState(value)

  function handleBlur() {
    setEditing(false)
    onSave(draft)
  }

  if (editing) {
    return (
      <textarea
        autoFocus
        value={draft}
        onChange={e => setDraft(e.target.value)}
        onBlur={handleBlur}
        rows={2}
        className="w-full resize-none rounded border border-blue-400 px-2 py-1 text-sm text-gray-900 focus:outline-none focus:ring-1 focus:ring-blue-500"
      />
    )
  }

  return (
    <span
      role="button"
      tabIndex={0}
      onClick={() => { setDraft(value); setEditing(true) }}
      onKeyDown={e => { if (e.key === 'Enter') { setDraft(value); setEditing(true) } }}
      className="block min-h-[2rem] cursor-text rounded px-1 py-0.5 text-sm text-gray-700 hover:bg-blue-50"
    >
      {value || <span className="text-gray-400 italic">Clique para editar</span>}
    </span>
  )
}

interface RowProps {
  aula: AulaGrade
  onUpdate: (campo: CampoEditavel, valor: string) => void
}

function SortableRow({ aula, onUpdate }: RowProps) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: aula.id })

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  }

  return (
    <tr ref={setNodeRef} style={style} className="border-b border-gray-200 hover:bg-gray-50">
      <td className="px-2 py-2 text-center">
        <button
          type="button"
          {...attributes}
          {...listeners}
          className="cursor-grab rounded p-1 text-gray-400 hover:text-gray-600 active:cursor-grabbing"
          aria-label="Arrastar linha"
        >
          <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
            <path d="M7 2a2 2 0 110 4 2 2 0 010-4zM7 8a2 2 0 110 4 2 2 0 010-4zM7 14a2 2 0 110 4 2 2 0 010-4zM13 2a2 2 0 110 4 2 2 0 010-4zM13 8a2 2 0 110 4 2 2 0 010-4zM13 14a2 2 0 110 4 2 2 0 010-4z" />
          </svg>
        </button>
      </td>
      <td className="px-3 py-2 text-center text-sm font-medium text-gray-600">{aula.semana}</td>
      <td className="px-3 py-2 text-center text-sm font-medium text-gray-600">{aula.aula}</td>
      <td className="px-3 py-2">
        <EditableCell value={aula.conteudo} onSave={v => onUpdate('conteudo', v)} />
      </td>
      <td className="px-3 py-2">
        <EditableCell value={aula.objetivos} onSave={v => onUpdate('objetivos', v)} />
      </td>
      <td className="px-3 py-2">
        <EditableCell value={aula.recursosSugeridos} onSave={v => onUpdate('recursosSugeridos', v)} />
      </td>
    </tr>
  )
}

export function GradeAulasTable({ aulas, onChange }: Props) {
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates }),
  )

  function handleDragEnd(event: DragEndEvent) {
    const { active, over } = event
    if (over && active.id !== over.id) {
      const oldIndex = aulas.findIndex(a => a.id === active.id)
      const newIndex = aulas.findIndex(a => a.id === over.id)
      onChange(arrayMove(aulas, oldIndex, newIndex))
    }
  }

  function handleUpdate(id: string, campo: CampoEditavel, valor: string) {
    onChange(aulas.map(a => a.id === id ? { ...a, [campo]: valor } : a))
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white">
      <table className="w-full border-collapse text-left">
        <thead className="bg-gray-50">
          <tr>
            <th className="w-10 px-2 py-3" />
            <th className="w-16 px-3 py-3 text-center text-xs font-semibold uppercase tracking-wide text-gray-500">Semana</th>
            <th className="w-14 px-3 py-3 text-center text-xs font-semibold uppercase tracking-wide text-gray-500">Aula</th>
            <th className="px-3 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500">Conteúdo</th>
            <th className="px-3 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500">Objetivos</th>
            <th className="px-3 py-3 text-xs font-semibold uppercase tracking-wide text-gray-500">Recursos Sugeridos</th>
          </tr>
        </thead>
        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
          <SortableContext items={aulas.map(a => a.id)} strategy={verticalListSortingStrategy}>
            <tbody>
              {aulas.map(aula => (
                <SortableRow
                  key={aula.id}
                  aula={aula}
                  onUpdate={(campo, valor) => handleUpdate(aula.id, campo, valor)}
                />
              ))}
            </tbody>
          </SortableContext>
        </DndContext>
      </table>
    </div>
  )
}
