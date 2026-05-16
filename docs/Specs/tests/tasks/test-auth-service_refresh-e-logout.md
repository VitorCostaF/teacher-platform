# Testes Unitários — AuthService: Refresh e Logout de Sessão

> **Escopo:** Backend — `AuthService.refresh` e `AuthService.logout`  
> **Tipo:** Backend  
> **Complexidade estimada:** M  
> **Depende de:** `test-auth-service_fluxo-login.md` (mesma classe de teste)

---

## Contexto

`refresh` e `logout` completam o ciclo de vida de uma sessão autenticada. O `refresh` implementa rotação de token (token antigo revogado, novo criado) com limite de sessões simultâneas. O `logout` revoga a sessão de forma segura. Ambos são críticos para a segurança — tokens não revogados representam risco de acesso indevido.

---

## O que deve ser implementado

Adicionar testes para `refresh` e `logout` na classe `AuthServiceTest`. O foco é a lógica de rotação de token, o limite de 5 sessões simultâneas e o comportamento defensivo do logout com tokens ausentes ou inválidos.

**Cenários do `refresh`:**
- Token null/vazio → `UnauthorizedException`
- Token inválido/expirado (`isTokenValid = false`) → `UnauthorizedException`
- Sessão não encontrada ou revogada → `UnauthorizedException`
- Refresh bem-sucedido: sessão atual revogada, nova criada, novos tokens gerados
- Com < 5 sessões ativas: nenhuma sessão extra revogada
- Com exatamente 5 sessões: a mais antiga revogada (exceto se for a atual)
- Com 5 sessões e a mais antiga = atual: não revoga a mais antiga

**Cenários do `logout`:**
- Token null → sem exceção, sem ação
- Token em branco → sem exceção, sem ação
- Sessão encontrada → `revogadoEm` preenchido, `save` chamado
- Sessão não encontrada → sem exceção, sem ação

---

## Critérios de Aceite

- [ ] `refresh(null)` lança `UnauthorizedException`
- [ ] `refresh("")` lança `UnauthorizedException`
- [ ] `refresh` com `isTokenValid = false` lança `UnauthorizedException`
- [ ] `refresh` sem sessão ativa lança `UnauthorizedException`
- [ ] `refresh` bem-sucedido: `sessaoRepository.save` chamado 2 vezes (revogar + criar)
- [ ] `refresh` bem-sucedido: `sessaoAtual.revogadoEm != null`
- [ ] `refresh` bem-sucedido: `jwtService.generateAccessToken` e `generateRefreshToken` chamados
- [ ] Com 5 sessões e mais antiga ≠ atual: mais antiga tem `revogadoEm` preenchido
- [ ] Com 5 sessões e mais antiga = atual: não revoga a mais antiga
- [ ] `logout(null)` não lança exceção e não chama `save`
- [ ] `logout("")` não lança exceção e não chama `save`
- [ ] `logout` com sessão válida: `sessao.revogadoEm` preenchido e `save` chamado
- [ ] `logout` sem sessão no repositório: não lança exceção

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/AuthService.md`
- **Seções:** `Método: refresh` e `Método: logout`

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/AuthServiceTest.java`

**Helper para criar lista de sessões:**
```java
private List<Sessao> sessoesAtivas(int quantidade, Sessao sessaoAtual) {
    List<Sessao> lista = new ArrayList<>();
    for (int i = 0; i < quantidade; i++) {
        var s = new Sessao();
        s.setId((long) i);
        lista.add(s);
    }
    lista.add(sessaoAtual); // sessão atual é a última
    return lista;
}
```

**Constante relevante:** `MAX_SESSOES_ATIVAS = 5` (campo private na classe)

---

## Notas e Edge Cases

- O `refresh` reutiliza `UserAgent` e IP da sessão anterior — não do request HTTP
- O limite de sessões compara por `id` — não por referência de objeto
- O hash do novo refresh token deve ser SHA-256 em Base64 (mesmo padrão do login)

---

## Definition of Done

- [ ] Todos os cenários de `refresh` e `logout` cobertos (mínimo 13 testes adicionais)
- [ ] Testes passam junto com os de `login` em `AuthServiceTest`
- [ ] Sem chamada real a banco
