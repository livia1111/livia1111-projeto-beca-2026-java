package com.nttdata.users.adapter.out.csv;

import com.nttdata.users.application.exception.BusinessValidationException;
import com.nttdata.users.application.port.in.CreateUserCommand;
import com.nttdata.users.application.port.out.UserCsvReader;
import com.nttdata.users.domain.model.Role;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class UserCsvReaderAdapter implements UserCsvReader {
    private static final List<String> CABECALHO = List.of("nome", "email", "role", "senha");
    private final Validator validator;

    public UserCsvReaderAdapter(Validator validator) { this.validator = validator; }

    @Override
    public void ler(InputStream arquivo, Consumer<Linha> consumidor) throws IOException {
        var decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT);
        // O controller é responsável por fechar o InputStream recebido.
        var reader = new BufferedReader(new InputStreamReader(arquivo, decoder));
        String cabecalho = reader.readLine();
        if (cabecalho == null) throw new BusinessValidationException("O arquivo está vazio.");
        if (cabecalho.startsWith("\uFEFF")) cabecalho = cabecalho.substring(1);
        validarCabecalho(cabecalho);
        String texto;
        int numero = 1;
        while ((texto = reader.readLine()) != null) {
            numero++;
            if (numero > 1001) throw new BusinessValidationException(
                    "Limite de 1.000 linhas excedido. Os cadastros anteriores foram mantidos.");
            // Parse separado por linha: aspas inválidas não consomem os próximos usuários.
            consumidor.accept(interpretar(texto, numero));
        }
    }

    private void validarCabecalho(String texto) {
        try {
            if (campos(texto).stream().map(String::strip).toList().equals(CABECALHO)) return;
        } catch (IOException | UncheckedIOException ignored) {
            // Retorna uma mensagem sem expor conteúdo do arquivo.
        }
        throw new BusinessValidationException("Cabeçalho inválido. Use nome;email;role;senha.");
    }

    private Linha interpretar(String texto, int numero) {
        List<String> valores;
        try {
            valores = campos(texto);
        } catch (IOException | UncheckedIOException ex) {
            return new Linha(numero, null, null, "CSV inválido. Verifique as aspas e use um usuário por linha.");
        }
        String email = valores.size() > 1 ? valores.get(1).strip() : null;
        if (valores.size() != 4) return new Linha(numero, null, email,
                "A linha deve conter exatamente nome, email, role e senha.");
        Role role;
        try {
            role = Role.valueOf(valores.get(2).strip());
        } catch (IllegalArgumentException ex) {
            return new Linha(numero, null, email, "O perfil deve ser ADMIN ou USER.");
        }
        // O adaptador CSV valida seu próprio formato de entrada, sem depender do adaptador HTTP.
        var entrada = new CsvUserInput(valores.get(0), valores.get(1), valores.get(3), role);
        String erros = validator.validate(entrada).stream()
                .map(v -> v.getMessage()).distinct().sorted().collect(Collectors.joining(" "));
        if (!erros.isEmpty()) return new Linha(numero, null, email, erros);
        return new Linha(numero, new CreateUserCommand(
                entrada.nome(), entrada.email(), entrada.senha(), entrada.role()), entrada.email(), null);
    }

    private List<String> campos(String texto) throws IOException {
        var formato = CSVFormat.RFC4180.builder().setDelimiter(';').get();
        try (var parser = formato.parse(new StringReader(texto))) {
            var registros = parser.getRecords();
            if (registros.size() != 1) return List.of();
            return registros.getFirst().toList();
        }
    }
}
