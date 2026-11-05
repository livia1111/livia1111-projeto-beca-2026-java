package com.nttdata.ms1.application.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {

    void execute(UUID id);
}
