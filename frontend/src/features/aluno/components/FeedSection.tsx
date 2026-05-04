interface Props {
  title: string
  children: React.ReactNode[]
  emptyMessage?: string
  forceShow?: boolean
}

export function FeedSection({ title, children, emptyMessage, forceShow = false }: Props) {
  const isEmpty = children.length === 0

  if (isEmpty && !forceShow) return null

  return (
    <section className="flex flex-col gap-3">
      <h2 className="text-sm font-semibold text-gray-700">{title}</h2>
      {isEmpty ? (
        <p className="rounded-xl border border-dashed border-gray-200 py-6 text-center text-sm text-gray-400">
          {emptyMessage ?? 'Nenhum item'}
        </p>
      ) : (
        <div className="flex flex-col gap-3">{children}</div>
      )}
    </section>
  )
}
