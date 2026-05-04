import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { alunoService } from '../services/aluno.service'
import type { FlashcardData } from '../types'

export function useFlashcards(cards: FlashcardData[]) {
  const navigate = useNavigate()
  const [indiceAtual, setIndiceAtual] = useState(0)
  const [respondidos, setRespondidos] = useState(0)
  const [isAvaliando, setIsAvaliando] = useState(false)
  const [sessaoEncerrada, setSessaoEncerrada] = useState(false)
  const [isFlipped, setIsFlipped] = useState(false)

  const cardAtual = cards[indiceAtual] ?? null

  async function responder(sabia: boolean) {
    if (!cardAtual || isAvaliando) return
    setIsAvaliando(true)
    try {
      await alunoService.avaliarFlashcard(cardAtual.id, sabia)
    } catch {
      // falha silenciosa — progresso salvo no servidor quando possível
    } finally {
      setIsAvaliando(false)
    }

    const proxIdx = indiceAtual + 1
    setRespondidos(r => r + 1)
    setIsFlipped(false)

    // pequeno delay para resetar a animação antes de trocar o card
    setTimeout(() => {
      if (proxIdx >= cards.length) {
        setSessaoEncerrada(true)
      } else {
        setIndiceAtual(proxIdx)
      }
    }, 300)
  }

  function encerrarSessao() {
    navigate(-1)
  }

  return {
    cardAtual,
    indiceAtual,
    respondidos,
    total: cards.length,
    isAvaliando,
    sessaoEncerrada,
    isFlipped,
    setIsFlipped,
    responder,
    encerrarSessao,
  }
}
