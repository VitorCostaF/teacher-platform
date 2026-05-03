# Plano de Implementação — global_offline-e-pwa

> **Task origem:** `docs/Tasks/global_offline-e-pwa.md`
> **Escopo:** Frontend — Global (PWA)
> **Complexidade:** G
> **Sprint:** 7 — Polimento e Infraestrutura
> **Depende de:** `aluno_realizacao-atividade-plan.md`

---

## Contexto do Codebase

`apiClient`, `toastEmitter`, `useProvaPlayer` (com lógica de backup offline parcial) já existem. Vite utilizado como bundler. Esta task formaliza o Service Worker, o manifest PWA e a infraestrutura de cache.

---

## Dependências a Adicionar

```bash
npm install vite-plugin-pwa workbox-window
```

---

## Arquivos a Criar

### Manifest

`frontend/public/manifest.json`
```json
{
  "name": "Teacher Platform",
  "short_name": "TeacherPlatform",
  "start_url": "/",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#2563eb",
  "icons": [
    { "src": "/icons/icon-192.png", "sizes": "192x192", "type": "image/png" },
    { "src": "/icons/icon-512.png", "sizes": "512x512", "type": "image/png" },
    { "src": "/icons/icon-512-maskable.png", "sizes": "512x512", "type": "image/png", "purpose": "maskable" }
  ]
}
```

Criar ícones em `frontend/public/icons/` (192px, 512px, 512px maskable).

### Configuração do Service Worker via vite-plugin-pwa

Atualizar `frontend/vite.config.ts`:
```typescript
import { VitePWA } from 'vite-plugin-pwa'

plugins: [
  react(),
  tailwindcss(),
  VitePWA({
    registerType: 'autoUpdate',
    workbox: {
      globPatterns: ['**/*.{js,css,html,ico,png,svg}'],
      runtimeCaching: [
        {
          urlPattern: /^\/api\/(?!auth\/|provas\/\d+\/sessoes)/,
          handler: 'NetworkFirst',
          options: { cacheName: 'api-cache', expiration: { maxEntries: 50, maxAgeSeconds: 300 } }
        },
        {
          urlPattern: /^\/api\/auth\//,
          handler: 'NetworkOnly',  // nunca cachear tokens
        },
      ],
    },
    manifest: false,  // usar manifest.json manual
  })
]
```

### Componente de Status de Conexão

`frontend/src/components/feedback/OfflineBanner.tsx`
- Monitorar `window.addEventListener('online' | 'offline')`
- Offline: banner laranja no topo "Você está sem conexão. Algumas funções podem não funcionar."
  - Aparece em < 1s após perda
- Reconnect: banner verde "Conexão restaurada!" por 3 segundos, depois desaparece
- Não bloqueia uso do app

### Hook de Sincronização

`frontend/src/hooks/useOfflineSync.ts`
- Ao reconectar: varrer `localStorage` por chaves `prova_backup_*` e `atividade_rascunho_*`
- Para cada backup encontrado: chamar endpoint de autosave correspondente
- Se sync bem-sucedido: remover da localStorage + toast "Suas respostas foram sincronizadas"
- Se sync falhar: manter backup + toast de aviso

### Registro do Service Worker

`frontend/src/lib/pwaRegister.ts`
- Usar `workbox-window` para registrar e checar atualizações
- Se nova versão disponível: toast "Nova versão disponível" com botão "Atualizar" que chama `workbox.messageSkipWaiting()`

---

## Arquivos a Modificar

| Arquivo | O que muda |
|---------|-----------|
| `frontend/vite.config.ts` | Adicionar VitePWA plugin com configuração de cache |
| `frontend/index.html` | Adicionar `<link rel="manifest">` e `<meta name="theme-color">` |
| `frontend/src/main.tsx` | Importar `pwaRegister.ts` e `<OfflineBanner>` no layout |

---

## Ordem de Implementação

```
1. Instalar vite-plugin-pwa, workbox-window
2. manifest.json + ícones
3. Configuração VitePWA no vite.config.ts (estratégias de cache)
4. OfflineBanner (testar com DevTools → Network → Offline)
5. useOfflineSync hook
6. pwaRegister.ts (update prompt)
7. Atualizar index.html + main.tsx
8. Testes: install em Android (Chrome), install em iOS (Safari via Add to Home Screen), offline → banner, reconexão → sync
9. Validar: tokens nunca cacheados (inspecionar Service Worker no DevTools)
```

---

## Checklist de Validação

- [ ] App instalável como PWA em Android e iOS
- [ ] Banner offline em < 1s após perda
- [ ] Banner verde 3s após reconexão
- [ ] Conteúdos acessados disponíveis offline
- [ ] Rascunhos preservados em localStorage offline
- [ ] Sync automático ao reconectar
- [ ] Tokens e sessão nunca cacheados

---

## Notas e Riscos

- iOS Safari tem limitações de Service Worker: não suporta Push API em iOS < 16.4. Testar exaustivamente.
- A sincronização de rascunho deve ser idempotente — o endpoint de autosave deve aceitar reenvios sem duplicar.
- Cache NetworkFirst para API tem TTL de 5 min — cuidado com dados obsoletos em telas de dashboard.

---

## Resumo

- **5 arquivos** a criar (manifest, OfflineBanner, useOfflineSync, pwaRegister, ícones)
- **3 arquivos** a modificar (vite.config.ts, index.html, main.tsx)
- **Dependências a adicionar:** vite-plugin-pwa, workbox-window
- **Complexidade mantida:** G
