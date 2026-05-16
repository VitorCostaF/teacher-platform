# Plataforma Educacional com IA — Especificação Técnica

> Documentação técnica completa da plataforma educacional. Serve como contrato entre produto, design, frontend e backend.

---

## Visão Geral

Plataforma web e mobile voltada para o ecossistema escolar, integrando professores, alunos, responsáveis e a coordenação pedagógica em um único ambiente. A inteligência artificial é um componente transversal — não um módulo isolado — presente na criação de conteúdo, correção, adaptação de nível e análise de desempenho.

**Problema resolvido:** Professores gastam tempo excessivo em tarefas operacionais (criar provas, preencher diários, montar materiais) e têm pouca visibilidade sobre o desempenho individual de cada aluno em tempo real. Alunos carecem de feedback imediato e material adaptado ao seu nível.

---

## Perfis de Usuário

| Perfil | Descrição |
|--------|-----------|
| **Professor** | Cria e gerencia turmas, provas, atividades e conteúdos |
| **Aluno** | Acessa conteúdos, realiza atividades e provas, acompanha seu desempenho |
| **Responsável** | Visualiza desempenho e frequência do aluno vinculado |
| **Coordenador** | Visão consolidada de todas as turmas e professores da escola |
| **Admin da Escola** | Cadastra professores, configura turmas e gerencia a instituição |

---

## Plataformas

- **Web** (desktop-first para professores e coordenadores)
- **PWA / Mobile** (mobile-first para alunos e responsáveis)
- Suporte offline para leitura de conteúdos e realização de atividades em cache

---

## Estrutura da Documentação

```
specs/
├── README.md                          → Este arquivo — visão geral e índice
│
├── 01-autenticacao.md                 → Login, cadastro, recuperação de senha
│
├── 02-area-professor/
│   ├── 02a-gestao-turmas.md           → Cadastro de turmas e alunos, frequência
│   ├── 02b-criacao-com-ia.md          → Provas, atividades, grade de aulas com IA
│   └── 02c-dashboard-desempenho.md    → Gráficos e relatórios de desempenho
│
├── 03-area-aluno/
│   ├── 03a-feed-conteudos.md          → Feed, leitura de materiais, flashcards
│   ├── 03b-provas-atividades.md       → Realização de provas e atividades online
│   └── 03c-desempenho-pessoal.md      → Histórico de notas e progresso
│
├── 04-area-responsavel.md             → Boletim, frequência, alertas
│
├── 05-area-administrativa.md          → Gestão de escola, professores, relatórios
│
├── 06-modelos-de-dados.md             → Schemas e relacionamentos
│
└── 07-comportamentos-globais.md       → Sessão, offline, erros, acessibilidade
```

---

## Glossário

| Termo | Definição |
|-------|-----------|
| **Turma** | Grupo de alunos associado a um professor e uma disciplina em um período letivo |
| **Atividade** | Tarefa enviada pelo professor, sem limite de tempo obrigatório |
| **Prova** | Avaliação com timer, janela de entrega e peso na nota final |
| **Grade de Aulas** | Planejamento semanal/semestral de conteúdos por disciplina |
| **BNCC** | Base Nacional Comum Curricular — referência de conteúdos por série |
| **Flashcard** | Cartão de revisão gerado por IA com pergunta e resposta |
| **Mapa de Calor** | Visualização de acertos/erros por tema, destacando dificuldades |

---

## Integrações Externas

| Serviço | Finalidade |
|---------|------------|
| **Anthropic Claude API** | Geração de provas, atividades, resumos, tutor virtual, correção de redações |
| **Google Classroom** | Importação/sincronização de turmas e alunos |
| **Microsoft Teams** | Importação/sincronização de turmas |
| **Provedor de e-mail (SendGrid)** | Envio de comunicados, alertas e convites |
| **Storage (S3-compatível)** | Upload e entrega de PDFs, imagens e vídeos |

---

## Comportamentos Globais

Ver [`07-comportamentos-globais.md`](07-comportamentos-globais.md) para especificação completa de:
- Sessão expirada
- Perda de conexão / modo offline
- Ações destrutivas (confirmação obrigatória)
- Estados de carregamento (skeleton vs spinner)
- Tratamento de erros de servidor
- Acessibilidade
