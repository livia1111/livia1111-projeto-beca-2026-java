package com.nttdata.users.adapter.out.persistence;

import com.nttdata.users.adapter.out.persistence.entity.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
    @Query("select u.email from UserJpaEntity u where u.email = :email")
    Optional<String> buscarEmail(@Param("email") String email);

    @Query("select u.id from UserJpaEntity u where u.email = :email")
    Optional<UUID> buscarIdPorEmail(@Param("email") String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserJpaEntity u where u.id = :id")
    Optional<UserJpaEntity> buscarParaAtualizar(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query("delete from UserJpaEntity u where u.id = :id")
    int excluir(@Param("id") UUID id);
}
