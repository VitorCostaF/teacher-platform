package br.com.inovadados.teacherplatform.service;

import br.com.inovadados.teacherplatform.domain.entity.PreferenciasNotificacao;
import br.com.inovadados.teacherplatform.domain.entity.Usuario;
import br.com.inovadados.teacherplatform.repository.PreferenciasNotificacaoRepository;
import br.com.inovadados.teacherplatform.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferenciasNotificacaoService {

    private final PreferenciasNotificacaoRepository preferenciasRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PreferenciasNotificacao getPreferencias(UUID usuarioId) {
        return preferenciasRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> criarComDefaults(usuarioId));
    }

    @Transactional
    public PreferenciasNotificacao atualizarPreferencias(UUID usuarioId, Map<String, Boolean> prefs) {
        PreferenciasNotificacao pref = getPreferencias(usuarioId);

        if (prefs.containsKey("faltaAluno")) pref.setFaltaAluno(prefs.get("faltaAluno"));
        if (prefs.containsKey("quedaFrequencia")) pref.setQuedaFrequencia(prefs.get("quedaFrequencia"));
        if (prefs.containsKey("prazoProva")) pref.setPrazoProva(prefs.get("prazoProva"));

        return preferenciasRepository.save(pref);
    }

    private PreferenciasNotificacao criarComDefaults(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado: " + usuarioId));

        PreferenciasNotificacao pref = new PreferenciasNotificacao();
        pref.setUsuario(usuario);
        pref.setFaltaAluno(true);
        pref.setQuedaFrequencia(true);
        pref.setPrazoProva(true);
        return preferenciasRepository.save(pref);
    }
}
