package com.nttdata.users.application.port.in;

import com.nttdata.users.domain.model.User;
import java.util.List;
import java.util.UUID;

public interface ManageUserUseCase {
    List<User> listar();
    User buscar(UUID id);
    User atualizar(UUID id, UpdateUserCommand command);
    void excluir(UUID id);
}
