# Spec 07 — Comportamentos Globais

> Comportamentos que se aplicam a toda a plataforma, independentemente de tela ou funcionalidade.  
> Esta spec deve ser lida por todos os times: frontend, backend e produto.

---

## 1. Sessão e Autenticação

### Sessão Expirada

- Access token tem validade de **1 hora**
- Antes de expirar, o frontend tenta renovar silenciosamente via `POST /auth/refresh`
- Se a renovação falhar (refresh token expirado ou inválido):
  - Salvar URL atual em `sessionStorage`
  - Redirecionar para `/login`
  - Após login bem-sucedido, redirecionar para a URL salva
  - Exibir toast: "Sua sessão expirou. Você foi redirecionado para o login."
- **Durante prova em andamento:** a renovação de token é obrigatória e não pode interromper a sessão. Se falhar, salvar respostas localmente e tentar submeter após reconexão.

### Múltiplas Abas

- Sessão é compartilhada entre abas do mesmo browser (mesmo token)
- Logout em uma aba deve propagar para todas as outras (BroadcastChannel API)

---

## 2. Perda de Conexão / Modo Offline

### Detecção

- Monitorar eventos `online` e `offline` do browser
- Ao perder conexão: exibir banner fixo no topo — "Sem conexão. Algumas funcionalidades podem estar indisponíveis."
- Ao reconectar: banner muda para verde "Conexão restaurada" por 3 segundos, depois some

### Comportamento por contexto

| Contexto | Comportamento offline |
|----------|-----------------------|
| Feed de conteúdos | Exibir conteúdos já carregados (cache Service Worker) |
| Leitura de material | Disponível se já foi aberto (cache) |
| Realizando atividade | Autosave em localStorage; sincronizar ao reconectar |
| Realizando prova | Autosave em localStorage; timer continua; alertar usuário |
| Lançando frequência | Bloquear envio; exibir aviso; dados locais preservados |
| Dashboard | Exibir dados cacheados com indicador "Dados podem estar desatualizados" |

### Service Worker

- Cachear: assets estáticos, conteúdos lidos recentemente, dados do último dashboard
- Não cachear: dados sensíveis de notas, dados em tempo real de prova em andamento

---

## 3. Ações Destrutivas

Toda ação que resulta em perda permanente de dados ou reversão custosa exige **confirmação explícita**.

### Padrão de Confirmação

1. Usuário clica na ação destrutiva (ex: "Remover aluno da turma")
2. Modal de confirmação aparece com:
   - Título claro do que será feito: "Remover João Silva da turma?"
   - Descrição das consequências: "O histórico do aluno será mantido, mas ele perderá acesso às atividades desta turma."
   - Para ações de alto risco: campo para digitar nome/confirmação textual
   - Botão de confirmação em **vermelho** com texto afirmativo: "Sim, remover"
   - Botão de cancelar em cinza: "Cancelar"
3. Após confirmação: executar ação com loading; não fechar modal durante loading
4. Em caso de erro: manter modal aberto com mensagem de erro inline

### Ações que requerem confirmação

| Ação | Nível | Requer digitação? |
|------|-------|-------------------|
| Remover aluno de turma | Médio | Não |
| Desativar conta de professor | Alto | Sim (nome do professor) |
| Encerrar turma | Alto | Não |
| Excluir prova (rascunho) | Médio | Não |
| Cancelar prova publicada | Crítico | Sim ("CANCELAR") |
| Resetar configurações da escola | Crítico | Sim ("CONFIRMAR") |

---

## 4. Estados de Carregamento

### Quando usar Skeleton

- Carregamento inicial de listas, tabelas e dashboards
- Carregamento de conteúdo principal da tela
- Duração esperada > 300ms

### Quando usar Spinner

- Ações pontuais (submit de formulário, carregamento de modal)
- Operações que o usuário iniciou explicitamente
- Duração esperada < 2s

### Quando usar Loading Bar (topo da tela)

- Navegação entre páginas (SPA)
- Operações em segundo plano

### Timeouts

- Requisições com timeout de **15 segundos**
- Após timeout: mensagem "A operação está demorando mais que o esperado. Verifique sua conexão e tente novamente."
- Oferecer botão "Tentar novamente" (não recarregar a página inteira)

---

## 5. Tratamento de Erros de Servidor

### Mensagens padrão por código HTTP

| Código | Mensagem ao usuário |
|--------|---------------------|
| 400 | Exibir erros de validação inline (campo a campo) |
| 401 | Renovar token automaticamente; se falhar, redirecionar para login |
| 403 | "Você não tem permissão para realizar esta ação." |
| 404 | "Este conteúdo não foi encontrado ou pode ter sido removido." |
| 409 | Mensagem específica do contexto (ex: "Este e-mail já está cadastrado.") |
| 422 | Mensagem específica da regra de negócio violada |
| 429 | "Muitas requisições. Aguarde alguns instantes antes de tentar novamente." |
| 500 | "Algo deu errado no servidor. Nossa equipe foi notificada. Tente novamente em alguns minutos." |
| 503 | "O serviço está temporariamente indisponível para manutenção. Tente novamente em breve." |
| Timeout | "A operação demorou muito para responder. Verifique sua conexão." |

### Apresentação de erros

- **Erros de campo (400/422):** Inline abaixo do campo, com ícone de alerta ⚠️
- **Erros de ação (403/404/500):** Toast no canto superior direito, duração 5 segundos, com botão X para fechar manualmente
- **Erros críticos que impedem uso:** Banner inline no centro da tela (não toast), com opção de retry
- **Nunca** mostrar stack traces, mensagens técnicas ou IDs de erro para o usuário final
- Logar todos os erros no frontend com contexto (rota, usuário, payload) para ferramentas de monitoramento (ex: Sentry)

---

## 6. Notificações Push

### Tipos de notificação

| Tipo | Exemplo | Prioridade |
|------|---------|------------|
| Lembrete | "Prova de Matemática amanhã às 9h" | Normal |
| Alerta | "Frequência do aluno abaixo de 75%" | Alta |
| Ação | "Atividade corrigida — veja sua nota" | Normal |
| Comunicado | "Reunião de pais na próxima semana" | Normal |

### Comportamento

- Usuário deve consentir com notificações push no primeiro acesso (não pedir imediatamente no login — aguardar interação)
- Notificações não lidas acumulam badge no ícone do app
- Clicar na notificação abre o contexto relevante (tela específica, não o feed genérico)
- Usuário pode configurar quais tipos de notificação deseja receber (Perfil > Notificações)

---

## 7. Acessibilidade

### Requisitos mínimos (WCAG 2.1 AA)

- **Contraste:** mínimo 4.5:1 para texto normal, 3:1 para texto grande
- **Foco visível:** todos os elementos interativos devem ter outline de foco visível (não remover `outline: none` sem substituto)
- **Navegação por teclado:** toda funcionalidade deve ser acessível via teclado. Ordem de Tab deve ser lógica
- **Labels:** todos os inputs devem ter `<label>` associado (não usar apenas placeholder)
- **Alt text:** imagens com conteúdo informativo devem ter `alt` descritivo; imagens decorativas devem ter `alt=""`
- **ARIA:** usar `aria-live` para atualizações dinâmicas (ex: contador de flashcards, resultado de ação)
- **Modais:** foco deve ir para o modal ao abrir e retornar ao elemento gatilho ao fechar; `Escape` fecha o modal
- **Tabelas:** usar `<th>` com `scope` e `<caption>` quando necessário

### Fontes e escalabilidade

- Tamanho de fonte base: 16px (nunca abaixo de 14px para texto de conteúdo)
- Interface deve ser utilizável com zoom do browser até 200%

---

## 8. LGPD e Privacidade

### Dados de menores

- Plataforma processa dados de menores de 18 anos — consentimento dos responsáveis é obrigatório
- Ao cadastrar aluno: responsável recebe e-mail de ciência e autorização antes do acesso ser liberado
- Dados de alunos não são usados para treinar modelos de IA externos sem consentimento explícito

### Direitos dos titulares

- Usuários (ou responsáveis, no caso de menores) podem solicitar via e-mail:
  - Acesso a seus dados
  - Correção de dados incorretos
  - Exclusão de dados (com prazo de 30 dias; dados de auditoria são preservados conforme lei)
  - Portabilidade dos dados (exportação em JSON/CSV)

### Retenção de dados

| Tipo de dado | Retenção |
|--------------|----------|
| Dados de conta | Enquanto ativo + 2 anos após desativação |
| Notas e frequência | Mínimo 5 anos (documentação escolar) |
| Logs de auditoria | Mínimo 5 anos |
| Conteúdos gerados por IA | Enquanto a escola estiver ativa |
| Tokens de sessão | 30 dias (refresh token) |

---

## 9. Performance

### Metas

| Métrica | Meta |
|---------|------|
| First Contentful Paint (FCP) | < 1.5s |
| Time to Interactive (TTI) | < 3.5s |
| Largest Contentful Paint (LCP) | < 2.5s |
| Cumulative Layout Shift (CLS) | < 0.1 |

### Estratégias obrigatórias

- Paginação server-side para listas > 20 itens
- Virtualização para listas muito longas (> 100 itens visíveis)
- Lazy loading de imagens e componentes pesados
- Compressão de assets (gzip/brotli)
- CDN para arquivos estáticos e uploads
- Skeleton loaders para evitar CLS

---

## 10. Internacionalização

- Idioma padrão: Português Brasileiro (pt-BR)
- Datas: formato DD/MM/AAAA
- Horas: formato 24h (HH:mm)
- Números decimais: vírgula como separador (ex: 7,5)
- Moeda (se aplicável): R$ com 2 casas decimais
- A arquitetura deve suportar i18n desde o início, mesmo que apenas pt-BR seja implementado inicialmente
