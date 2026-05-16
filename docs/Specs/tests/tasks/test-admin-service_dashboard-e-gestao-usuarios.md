# Testes Unitários — AdminService: Dashboard e Gestão de Usuários

> **Escopo:** Backend — `AdminService`  
> **Tipo:** Backend  
> **Complexidade estimada:** G  
> **Depende de:** Nenhuma

---

## Contexto

`AdminService` é o serviço mais amplo do sistema — cobre o painel administrativo, convite de professores, criação de alunos, transferência entre turmas e importação via planilha. Os testes devem cobrir as regras de isolamento de escola, a idempotência de convites e os cálculos do dashboard.

---

## O que deve ser implementado

Criar `AdminServiceTest` cobrindo: `getDashboard` (cálculos de métricas e alertas), `convidarProfessor` (idempotência), `alterarStatusProfessor` (isolamento de escola), `transferirAluno` e os contadores de importação.

---

## Critérios de Aceite

**`getDashboard` — cálculo de média:**
- [ ] Sem notas → média = `0.00`
- [ ] Uma nota `8.0` → média = `8.00`
- [ ] Notas `[6.0, 8.0, 10.0]` → média = `8.00`

**`getDashboard` — alertas de frequência:**
- [ ] Aluno com 25%+ de ausências → alerta `RISCO_REPROVACAO` gerado
- [ ] Aluno com 24.9% de ausências → sem alerta
- [ ] Sem aulas registradas → sem alerta (proteção divisão por zero)

**`convidarProfessor`:**
- [ ] Email já cadastrado → não cria novo usuário, envia novo token/convite
- [ ] Email novo → cria usuário com `ativo=false`, `perfil=PROFESSOR`
- [ ] Token salvo com tipo `CONVITE`
- [ ] `emailService` chamado com link de ativação contendo `baseUrl`
- [ ] `auditoriaService` chamado após convite

**`alterarStatusProfessor`:**
- [ ] Professor não encontrado → exceção
- [ ] Professor de outra escola → `AcessoNegadoException`
- [ ] `ativo=true` → `usuario.ativo = true`
- [ ] `ativo=false` → `usuario.ativo = false`
- [ ] `auditoriaService` chamado

**`transferirAluno`:**
- [ ] Turma destino não encontrada → `TurmaNaoEncontradaException`
- [ ] Matrícula anterior: `removidoEm = now`
- [ ] Nova matrícula criada para turma destino
- [ ] `auditoriaService` chamado

**`importarProfessores` / `importarAlunos`:**
- [ ] `planilhaParserService.parse` chamado
- [ ] Email existente → não duplica usuário
- [ ] Email novo → cria usuário
- [ ] `ImportacaoResponse` com contadores `criados` e `existentes`

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/AdminService.md`
- **Seções:** Todos os métodos documentados

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/AdminServiceTest.java`

```java
@BeforeEach
void setUp() {
    ReflectionTestUtils.setField(adminService, "baseUrl", "http://localhost:5173");
}
```

---

## Notas e Edge Cases

- O alerta de frequência usa `ausencias / totalAulas * 100` — threshold é `>= 25%`
- Convite idempotente: verificar que `usuarioRepository.save` não é chamado se o usuário já existe
- Isolamento de escola: `usuario.escola.id` deve ser igual a `escolaId` do admin

---

## Definition of Done

- [ ] Classe `AdminServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 22 testes)
- [ ] Testes passam com `./mvnw test -Dtest=AdminServiceTest`
