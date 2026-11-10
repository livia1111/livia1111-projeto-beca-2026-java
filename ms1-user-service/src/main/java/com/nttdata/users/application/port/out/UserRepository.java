package com.nttdata.users.application.port.out;

import com.nttdata.users.domain.model.User;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface UserRepository {
    Optional<String> buscarEmail(String email);
    User salvar(User user);
    Optional<User> buscarPorId(UUID id);
    Optional<User> buscarPorEmailCompleto(String email);
    List<User> listar();
    Optional<UUID> buscarIdPorEmail(String email);
    boolean atualizar(User user);
    boolean excluir(UUID id);
}
