# Backend IA — Endpoints de Geração de Prova e Atividade

> **Escopo:** backend-ia  
> **Tipo:** Backend  
> **Complexidade estimada:** G  
> **Depende de:** `backend-model_avaliacoes.md`

---

## Contexto

Integração com a API da IA (Claude) para geração de questões. Inclui rate limiting por professor, upload de conteúdo e regeneração de questão individual.

---

## O que deve ser implementado

- `POST /ia/gerar-prova` — recebe configuração e conteúdo, monta prompt estruturado, chama API de IA, retorna array de questões com gabarito. Rate limit: 20 gerações/hora por professor.
- `POST /ia/regenerar-questao` — regenera uma questão específica mantendo contexto da prova
- `POST /upload/conteudo` — recebe PDF ou DOCX, extrai texto (via biblioteca), retorna texto limpo. Limite: 10MB.
- `POST /ia/gerar-grade` — gera grade de aulas dado série, disciplina, período e tópicos
- `POST /ia/gerar-flashcards` — gera flashcards a partir de um conteúdo
- Logar uso de tokens por professor e escola para controle de custo

---

## Critérios de Aceite

- [ ] `POST /ia/gerar-prova` retorna questões no formato especificado por tipo
- [ ] Rate limit 429 retornado com `retryAfter` correto
- [ ] Upload extrai texto corretamente de PDF e DOCX
- [ ] Aviso retornado se conteúdo extraído < 100 palavras úteis
- [ ] Log de uso de tokens salvo no banco
- [ ] Erros da API de IA são tratados e retornam 502 com mensagem amigável

---

## Especificação de Referência

- **Arquivo:** `02-area-professor/02b-criacao-com-ia.md`
- **Seção:** `Endpoints: Criação com IA`

---

## Detalhes Técnicos

**Response da geração:**
```json
{
  "questoes": [
    {
      "id": "temp-uuid",
      "tipo": "multipla_escolha",
      "enunciado": "...",
      "alternativas": ["a", "b", "c", "d"],
      "gabarito": 0,
      "dificuldade": "medio",
      "topico": "Frações"
    }
  ],
  "tokensUsados": 1240
}
```

---

## Notas e Edge Cases

- Prompt deve sempre incluir instrução para gerar em português brasileiro
- Se a IA retornar JSON malformado: tentar parse parcial; se falhar, retornar 502
- Conteúdo enviado pelo usuário deve ser sanitizado antes de ir para o prompt

---

## Definition of Done

- [ ] Endpoints implementados com tratamento de erro da IA
- [ ] Rate limiting implementado com Redis
- [ ] Testes com mocks da API de IA
- [ ] Code review realizado
