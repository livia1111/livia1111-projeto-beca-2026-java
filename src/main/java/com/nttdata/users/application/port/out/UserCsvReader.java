package com.nttdata.users.application.port.out;

import com.nttdata.users.application.port.in.CreateUserCommand;
import java.io.InputStream;
import java.io.IOException;
import java.util.function.Consumer;

public interface UserCsvReader {
    void ler(InputStream arquivo, Consumer<Linha> consumidor) throws IOException;

    record Linha(int numero, CreateUserCommand comando, String email, String erro) {
        @Override
        public String toString() { return "Linha[numero=" + numero + "]"; }
    }
}
