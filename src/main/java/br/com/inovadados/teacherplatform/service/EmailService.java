package br.com.inovadados.teacherplatform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    public void enviarConviteProfessor(String email, String nome, String linkConvite) {
        log.info("[EMAIL] Convite professor → {} <{}> | link: {}", nome, email, linkConvite);
    }

    public void enviarConviteAluno(String email, String nome, String linkConvite) {
        log.info("[EMAIL] Convite aluno → {} <{}> | link: {}", nome, email, linkConvite);
    }

    public void enviarConviteResponsavel(String email, String nome, String linkConvite) {
        log.info("[EMAIL] Convite responsável → {} <{}> | link: {}", nome, email, linkConvite);
    }
}
