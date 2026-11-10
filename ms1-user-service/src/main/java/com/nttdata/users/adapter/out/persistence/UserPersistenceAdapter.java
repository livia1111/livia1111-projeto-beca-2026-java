package com.nttdata.users.adapter.out.persistence;

import com.nttdata.users.adapter.out.persistence.entity.UserJpaEntity;
import com.nttdata.users.application.port.out.UserRepository;
import com.nttdata.users.domain.model.User;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public class UserPersistenceAdapter implements UserRepository {
    private final UserJpaRepository repository;
    private final UserPersistenceExceptionTranslator translator;
    private final TransactionTemplate transaction;

    public UserPersistenceAdapter(UserJpaRepository repository, UserPersistenceExceptionTranslator translator,
                                  TransactionTemplate transaction) {
        this.repository = repository;
        this.translator = translator;
        this.transaction = transaction;
    }

    @Override
    public Optional<String> buscarEmail(String email) {
        return repository.buscarEmail(email);
    }

    @Override
    public Optional<User> buscarPorId(UUID id) {
        return repository.findById(id).map(this::paraDominio);
    }

    @Override
    public Optional<User> buscarPorEmailCompleto(String email) {
        return repository.findByEmail(email).map(this::paraDominio);
    }

    @Override
    public List<User> listar() {
        return repository.findAll().stream().map(this::paraDominio).toList();
    }

    @Override
    public Optional<UUID> buscarIdPorEmail(String email) {
        return repository.buscarIdPorEmail(email);
    }

    @Override
    public boolean atualizar(User user) {
        try {
            return Boolean.TRUE.equals(transaction.execute(status -> {
                var encontrada = repository.buscarParaAtualizar(user.getId());
                if (encontrada.isEmpty()) {
                    return false;
                }
                UserJpaEntity entity = encontrada.get();
                entity.atualizarDados(user.getNome(), user.getEmail(), user.getSenha(), user.getUpdatedAt());
                repository.saveAndFlush(entity);
                return true;
            }));
        } catch (DataIntegrityViolationException ex) {
            throw translator.translate(ex);
        }
    }

    @Override
    public boolean excluir(UUID id) {
        return repository.excluir(id) > 0;
    }

    private User paraDominio(UserJpaEntity entity) {
        return User.reconstituir(entity.getId(), entity.getNome(), entity.getEmail(),
                entity.getSenhaHash(), entity.getRole(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    @Override
    public User salvar(User user) {
        try {
            UserJpaEntity entity = new UserJpaEntity(user.getId(), user.getNome(), user.getEmail(),
                    user.getSenha(), user.getRole(), user.getCreatedAt(), user.getUpdatedAt());
            // Flush aplica a constraint antes de sair do adaptador.
            UserJpaEntity saved = repository.saveAndFlush(entity);
            return User.reconstituir(saved.getId(), saved.getNome(), saved.getEmail(),
                    saved.getSenhaHash(), saved.getRole(), saved.getCreatedAt(), saved.getUpdatedAt());
        } catch (DataIntegrityViolationException ex) {
            throw translator.translate(ex);
        }
    }
}
