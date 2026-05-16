# Testes Unitários — TurmaService: Controle de Acesso e Matrícula

> **Escopo:** Backend — `TurmaService`  
> **Tipo:** Backend  
> **Complexidade estimada:** P  
> **Depende de:** Nenhuma

---

## Contexto

`TurmaService` controla o CRUD de turmas e a matrícula de alunos. O controle de acesso por perfil (professor só acessa suas turmas, admin acessa todas) e a idempotência de matrícula (reativar ao invés de duplicar) são as regras mais críticas a cobrir.

---

## O que deve ser implementado

Criar `TurmaServiceTest` cobrindo os métodos principais: listagem com filtro por período, busca com controle de acesso, e matrícula/remoção de alunos.

---

## Critérios de Aceite

**`buscarTurma`:**
- [ ] Turma não encontrada → `TurmaNaoEncontradaException`
- [ ] Professor acessa própria turma (`isAdmin=false`) → ok
- [ ] Professor acessa turma alheia (`isAdmin=false`) → `AcessoNegadoException`
- [ ] Admin acessa turma alheia (`isAdmin=true`) → ok
- [ ] `naoCorrigidas` = número de entregas com status `ENTREGUE`

**`listarTurmasProfessor`:**
- [ ] `periodoId = null` → chama `findByProfessorIdAndDeletadoEmIsNull`
- [ ] `periodoId != null` → chama `findByProfessorIdAndPeriodoLetivoIdAndDeletadoEmIsNull`
- [ ] Sem turmas → lista vazia

**`listarTurmasAdmin`:**
- [ ] `periodoId = null` → chama `findByEscolaIdAndDeletadoEmIsNull`
- [ ] `periodoId != null` → chama `findByEscolaIdAndPeriodoLetivoIdAndDeletadoEmIsNull`

**`adicionarAluno`:**
- [ ] Matrícula nova → cria com `matriculadoEm = now`
- [ ] Aluno já matriculado (ativo, `removidoEm = null`) → não cria duplicata
- [ ] Aluno com matrícula removida → reativa: `removidoEm = null`

**`removerAluno`:**
- [ ] Remove de forma lógica: `matricula.removidoEm = now` (não deleta do banco)
- [ ] Acesso negado para professor de outra turma → `AcessoNegadoException`

**`criarTurma`:**
- [ ] Período letivo não encontrado → `IllegalArgumentException`

---

## Especificação de Referência

- **Spec:** `docs/Specs/tests/unit/TurmaService.md`
- **Seções:** Todos os métodos documentados

---

## Detalhes Técnicos

**Localização:** `src/test/java/br/com/inovadados/teacherplatform/service/TurmaServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class TurmaServiceTest {
    @Mock TurmaRepository turmaRepository;
    @Mock MatriculaRepository matriculaRepository;
    @Mock EntregaRepository entregaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PeriodoLetivoRepository periodoLetivoRepository;
    @Mock PlanilhaParserService planilhaParserService;
    @InjectMocks TurmaService turmaService;
}
```

---

## Notas e Edge Cases

- Remoção é lógica (`deletadoEm` / `removidoEm`) — nunca usar `delete` do repositório
- Matrícula reativada limpa `removidoEm` — verificar que não cria nova entrada

---

## Definition of Done

- [ ] Classe `TurmaServiceTest` criada
- [ ] Todos os cenários cobertos (mínimo 15 testes)
- [ ] Testes passam com `./mvnw test -Dtest=TurmaServiceTest`
