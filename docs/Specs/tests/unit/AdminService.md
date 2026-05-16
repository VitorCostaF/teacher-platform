# Spec de Testes Unitários — AdminService

**Classe:** `br.com.inovadados.teacherplatform.service.AdminService`  
**Arquivo:** `src/main/java/br/com/inovadados/teacherplatform/service/AdminService.java`

---

## Visão Geral

`AdminService` concentra as operações de gestão escolar: convite de professores, criação de alunos, importação por planilha, transferência entre turmas e configuração da escola. Os testes devem cobrir a idempotência de convites, os cálculos do dashboard e as regras de importação.

**Dependências para mock:** `UsuarioRepository`, `EscolaRepository`, `TurmaRepository`, `MatriculaRepository`, `AvaliacaoRepository`, `EntregaRepository`, `RegistroFrequenciaRepository`, `TokenTemporarioRepository`, `LogAuditoriaRepository`, `VinculoResponsavelRepository`, `PlanilhaParserService`, `AuditoriaService`, `EmailService`.

---

## Método: `getDashboard(Long escolaId)`

### Cálculo de média de notas

| # | Cenário | Notas | Média Esperada |
|---|---------|-------|---------------|
| 1 | Sem notas | lista vazia | `0.00` |
| 2 | Uma nota | `[8.0]` | `8.00` |
| 3 | Múltiplas notas | `[6.0, 8.0, 10.0]` | `8.00` |
| 4 | Notas com casas decimais | `[7.5, 8.5]` | `8.00` |

### Cálculo de frequência geral

| # | Cenário | Dados | % Esperada |
|---|---------|-------|------------|
| 5 | Sem aulas | `totalAulas = 0` | `100.0` |
| 6 | Todos presentes | 10 aulas, 10 presenças por aluno | `100.0` |
| 7 | Metade presente | 10 aulas, 5 presenças por aluno | `50.0` |

### Alertas de frequência do dashboard

| # | Cenário | % Ausência do Aluno | Alerta Gerado? |
|---|---------|---------------------|----------------|
| 8 | Ausência >= 25% | `25%` | sim (`RISCO_REPROVACAO`) |
| 9 | Ausência < 25% | `24.9%` | não |
| 10 | Sem aulas registradas | `totalAulas = 0` | não (divisão por zero protegida) |

---

## Método: `convidarProfessor(ConvidarProfessorRequest, Long escolaId, HttpServletRequest)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Email já cadastrado na escola | `usuarioRepository.findByEmail` retorna usuário | envia novo convite sem criar usuário duplicado |
| 2 | Email novo | `findByEmail` retorna empty | cria novo usuário com `ativo=false`, envia convite |
| 3 | Token gerado | qualquer | `tokenTemporarioRepository.save` chamado com token do tipo `CONVITE` |
| 4 | Email enviado | qualquer | `emailService` chamado com link de ativação |
| 5 | Auditoria registrada | convite ok | `auditoriaService` chamado |

---

## Método: `alterarStatusProfessor(UUID id, AlterarStatusProfessorRequest, Long escolaId, UUID adminId, HttpServletRequest)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Professor não encontrado | `usuarioRepository.findById` retorna empty | lança exceção |
| 2 | Professor de outra escola | `usuario.escola.id ≠ escolaId` | lança `AcessoNegadoException` |
| 3 | Ativa professor | `request.ativo = true` | `usuario.ativo = true` |
| 4 | Desativa professor | `request.ativo = false` | `usuario.ativo = false` |
| 5 | Auditoria registrada | alteração ok | `auditoriaService` chamado |

---

## Método: `transferirAluno(UUID alunoId, TransferirAlunoRequest, Long escolaId, UUID adminId, HttpServletRequest)`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Turma destino não encontrada | `turmaRepository.findById` retorna empty | lança `TurmaNaoEncontradaException` |
| 2 | Matrícula anterior encerrada | matrícula existente na turma anterior | `matricula.removidoEm = now` |
| 3 | Nova matrícula criada | transferência ok | `matriculaRepository.save` chamado com nova turma |
| 4 | Auditoria registrada | transferência ok | `auditoriaService` chamado |

---

## Método: `importarProfessores` e `importarAlunos`

| # | Cenário | Condições | Comportamento |
|---|---------|-----------|---------------|
| 1 | Arquivo parsed via PlanilhaParserService | arquivo válido | `planilhaParserService.parse` chamado |
| 2 | Usuário já existente — não duplica | email já no banco | usa usuário existente, não cria novo |
| 3 | Usuário novo — cria com ativo=false | email novo | cria e envia convite |
| 4 | Retorna `ImportacaoResponse` com contadores | 3 criados, 1 existente | `criados=3`, `existentes=1` |

---

## Regras de Negócio Críticas

- Professores convidados são criados com `ativo=false` até aceitarem o convite.
- A verificação de escola garante isolamento — admin não acessa dados de outras escolas.
- Transferência de aluno é uma operação atômica: encerra matrícula anterior e cria nova.
- Toda operação sensível é registrada via `AuditoriaService`.

---

## Exemplo de Setup

```java
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock EscolaRepository escolaRepository;
    @Mock TurmaRepository turmaRepository;
    @Mock MatriculaRepository matriculaRepository;
    @Mock AvaliacaoRepository avaliacaoRepository;
    @Mock EntregaRepository entregaRepository;
    @Mock RegistroFrequenciaRepository frequenciaRepository;
    @Mock TokenTemporarioRepository tokenTemporarioRepository;
    @Mock LogAuditoriaRepository logAuditoriaRepository;
    @Mock VinculoResponsavelRepository vinculoResponsavelRepository;
    @Mock PlanilhaParserService planilhaParserService;
    @Mock AuditoriaService auditoriaService;
    @Mock EmailService emailService;

    @InjectMocks AdminService adminService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminService, "baseUrl", "http://localhost:5173");
    }
}
```
