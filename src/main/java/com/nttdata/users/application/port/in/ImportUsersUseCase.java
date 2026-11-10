package com.nttdata.users.application.port.in;

import java.io.InputStream;

public interface ImportUsersUseCase {
    ImportUsersResult importar(InputStream arquivo);
}
