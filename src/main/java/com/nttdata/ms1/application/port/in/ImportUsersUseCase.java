package com.nttdata.ms1.application.port.in;

import java.io.InputStream;
import java.util.List;

public interface ImportUsersUseCase {

    Result execute(InputStream csvInputStream);

    record Result(
            int totalRows,
            int imported,
            int failed,
            List<RowError> errors
    ) {
    }

    record RowError(
            int line,
            String email,
            String reason
    ) {
    }
}
