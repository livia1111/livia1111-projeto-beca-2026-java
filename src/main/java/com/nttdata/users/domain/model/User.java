package com.nttdata.users.domain.model;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import java.util.Locale;

/** Modelo de dados com fabricas explicitas para criacao e reconstituicao. */
public final class User {

    private final UUID id;
    private final String nome;
    private final String email;
    private final String senha;
    private final Role role;
    private final Instant createdAt;
    private final Instant updatedAt;

    /** O argumento senha deve conter somente o hash, nunca a senha original. */
    private User(UUID id, String nome, String email, String senha, Role role,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.nome = nome;
        this.email = email == null ? null : email.strip().toLowerCase(Locale.ROOT);
        this.senha = senha;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** Recebe a senha ja transformada em hash pela porta PasswordHasher. */
    public static User criar(String nome, String email, String senhaHash,
                             Role role, Clock clock) {
        Instant now = Instant.now(clock);
        return new User(UUID.randomUUID(), nome, email, senhaHash, role, now, now);
    }

    /** Preserva a identidade e as datas recuperadas da persistencia. */
    public static User reconstituir(UUID id, String nome, String email,
                                    String senhaHash, Role role,
                                    Instant createdAt, Instant updatedAt) {
        return new User(id, nome, email, senhaHash, role, createdAt, updatedAt);
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public Role getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    /** Dois usuarios sao iguais quando possuem o mesmo ID. */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
