package com.nttdata.users.application.port.in;

import java.util.List;

public record ImportUsersResult(int cadastrados, int rejeitados, List<Linha> resultados) {
    public ImportUsersResult {
        resultados = List.copyOf(resultados);
    }

    public record Linha(int linha, String email, String status, int codigo, String mensagem) {}

    public static ImportUsersResult de(List<Linha> linhas) {
        int cadastrados = (int) linhas.stream().filter(l -> l.codigo() == 201).count();
        return new ImportUsersResult(cadastrados, linhas.size() - cadastrados, linhas);
    }
}
