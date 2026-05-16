# Spec de Testes Unitários — TurmaService

**Classe:** `br.com.inovadados.teacherplatform.service.TurmaService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/TurmaService.java`

---

## Visão Geral

`TurmaService` gerencia o CRUD de turmas, matrícula de alunos e importação via planilha. Os testes devem cobrir o controle de acesso por perfil, a idempotência de matrícula e a remoção lógica.

**Dependências para mock:** `TurmaRepository`, `MatriculaRepository`, `EntregaRepository`, `UsuarioRepository`, `PeriodoLetivoRepository`, `PlanilhaParserService`.

---

## Método: `buscarTurma(Long turmaId, UUID usuarioId, boolean isAdmin)`

| # | Cenário | Condições | Saída Esperada |
|---|---------|-----------|----------------|
| 1 | Turma não encontrada | `turmaRepository.findById` retorna empty | lança `TurmaNaoEncontradaException` |
| 2 | Professor acessa sua própria turma | `turma.professor.id == usuarioId`, `isAdmin=false` | retorna `TurmaDetalheResponse` |
| 3 | Professor acessa turma alheia | `turma.professor.id ≠ usuarioId`, `isAdmin=false` | lança `AcessoNegadoException` |
| 4 | Admin acessa qualquer turma | `turma.professor.id ≠ usuarioId`, `isAdmin=true` | retorna `TurmaDetalheResponse` |
| 5 | `naoCorrigidas` contado corretamente | 3 entregas com status ENTREGUE | `pendencias.naoCorrigidas = 3` |

---

## Método: `listarTurmasProfessor(UUID professorId, Long periodoId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Sem filtro de período | `periodoId = null` | usa `findByProfessorIdAndDeletadoEmIsNull` |
| 2 | Com filtro de período | `periodoId = 1L` | usa `findByProfessorIdAndPeriodoLetivoIdAndDeletadoEmIsNull` |
| 3 | Sem turmas | lista vazia | retorna lista vazia |

---

## Método: `listarTurmasAdmin(Long escolaId, Long periodoId)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Sem filtro de período | `periodoId = null` | usa `findByEscolaIdAndDeletadoEmIsNull` |
| 2 | Com filtro de período | `periodoId = 2L` | usa `findByEscolaIdAndPeriodoLetivoIdAndDeletadoEmIsNull` |

---

## Método: `criarTurma(CriarTurmaRequest)`

| # | Cenário | Condições | Saída Esperada |
|---|---------|-----------|----------------|
| 1 | Período letivo não encontrado | `periodoLetivoRepository.findById` retorna empty | lança `IllegalArgumentException` |
| 2 | Turma criada com dados corretos | dados válidos | `turmaRepository.save` chamado; retorna detalhe da turma |

---

## Método: `adicionarAluno(Long turmaId, AdicionarAlunoRequest, UUID usuarioId, boolean isAdmin)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Aluno já matriculado (ativo) | matrícula existente com `removidoEm=null` | retorna sem criar nova matrícula |
| 2 | Aluno re-matriculado (remoção anterior) | matrícula com `removidoEm != null` | `removidoEm` limpo, matrícula reativada |
| 3 | Primeira matrícula | sem matrícula anterior | nova matrícula criada com `matriculadoEm = now` |

---

## Método: `removerAluno(Long turmaId, UUID alunoId, UUID usuarioId, boolean isAdmin)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Remoção lógica | matrícula ativa | `matricula.removidoEm = now` (não deleta do banco) |
| 2 | Acesso negado | professor de outra turma, `isAdmin=false` | lança `AcessoNegadoException` |
| 3 | Matrícula não encontrada | nenhuma matrícula para o aluno | lança `IllegalArgumentException` ou similar |

---

## Regras de Negócio Críticas

- Turmas são removidas de forma **lógica** (`deletadoEm`) — nunca deletadas do banco.
- Matrículas são removidas de forma **lógica** (`removidoEm`) — reativação limpa o campo.
- Professor só acessa suas próprias turmas — admin acessa todas da escola.
- `naoCorrigidas` conta entregas com status `ENTREGUE` (sem nota), não `CORRIGIDA`.

---

## Exemplo de Setup

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
