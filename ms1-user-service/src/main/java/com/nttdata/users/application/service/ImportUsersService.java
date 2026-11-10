package com.nttdata.users.application.service;

import com.nttdata.users.application.exception.BusinessValidationException;
import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import com.nttdata.users.application.exception.ImportInterruptedException;
import com.nttdata.users.application.port.in.*;
import com.nttdata.users.application.port.out.UserCsvReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImportUsersService implements ImportUsersUseCase {
    private final UserCsvReader reader;
    private final CreateUserUseCase cadastro;

    public ImportUsersService(UserCsvReader reader, CreateUserUseCase cadastro) {
        this.reader = reader;
        this.cadastro = cadastro;
    }

    @Override
    public ImportUsersResult importar(InputStream arquivo) {
        List<ImportUsersResult.Linha> resultados = new ArrayList<>();
        // Não envolver o arquivo inteiro em uma transação: cada cadastro é independente.
        try {
            reader.ler(arquivo, linha -> {
                if (linha.erro() != null) {
                    resultados.add(new ImportUsersResult.Linha(linha.numero(), normalizarEmail(linha.email()),
                            "REJEITADO", 400, linha.erro()));
                    return;
                }
                String email = normalizarEmail(linha.email());
                try {
                    cadastro.criar(linha.comando());
                    resultados.add(new ImportUsersResult.Linha(linha.numero(), email,
                            "CADASTRADO", 201, "Usuário cadastrado com sucesso."));
                } catch (EmailAlreadyExistsException ex) {
                    resultados.add(new ImportUsersResult.Linha(linha.numero(), email,
                            "REJEITADO", 409, ex.getMessage()));
                } catch (BusinessValidationException ex) {
                    resultados.add(new ImportUsersResult.Linha(linha.numero(), email,
                            "REJEITADO", 400, ex.getMessage()));
                }
            });
        } catch (BusinessValidationException ex) {
            throw new ImportInterruptedException(ex.getMessage(), 400, ImportUsersResult.de(resultados));
        } catch (IOException | RuntimeException ex) {
            // Falhas de infraestrutura não são erros de uma linha. Preservamos o relatório confirmado.
            throw new ImportInterruptedException(
                    "Importação interrompida. Os cadastros já confirmados foram mantidos. "
                    + "Consulte o relatório antes de reenviar o arquivo.",
                    500, ImportUsersResult.de(resultados));
        }
        return ImportUsersResult.de(resultados);
    }

    private String normalizarEmail(String email) {
        return email == null || email.isBlank() ? null : email.strip().toLowerCase(Locale.ROOT);
    }
}
