package com.nttdata.ms1.application.service;

import com.nttdata.ms1.application.port.in.ImportUsersUseCase;
import com.nttdata.ms1.application.port.out.UserRepositoryPort;
import com.nttdata.ms1.domain.model.User;
import com.nttdata.ms1.domain.model.UserEmail;
import com.nttdata.ms1.domain.model.UserName;
import com.nttdata.ms1.domain.model.UserPassword;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportUsersService implements ImportUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public ImportUsersService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public Result execute(InputStream csvInputStream) {
        List<RowError> errors = new ArrayList<>();
        int totalRows = 0;
        int imported = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvInputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (lineNumber == 1 && looksLikeHeader(line)) {
                    continue;
                }

                if (line.isBlank()) {
                    continue;
                }

                totalRows++;
                try {
                    ImportedRow row = parseLine(line, lineNumber);

                    UserEmail email = new UserEmail(row.email());
                    if (userRepositoryPort.existsByEmail(email)) {
                        errors.add(new RowError(lineNumber, email.value(), "email already registered"));
                        continue;
                    }

                    User user = User.createNew(
                            new UserName(row.name()),
                            email,
                            new UserPassword(row.password())
                    );
                    userRepositoryPort.save(user);
                    imported++;
                } catch (Exception exception) {
                    String emailHint = extractEmailHint(line);
                    errors.add(new RowError(lineNumber, emailHint, exception.getMessage()));
                }
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("unable to read csv file", exception);
        }

        int failed = errors.size();
        return new Result(totalRows, imported, failed, errors);
    }

    private boolean looksLikeHeader(String line) {
        String normalized = line.trim().toLowerCase();
        return normalized.equals("name,email,password");
    }

    private ImportedRow parseLine(String line, int lineNumber) {
        String[] parts = line.split(",", -1);
        if (parts.length != 3) {
            throw new IllegalArgumentException("invalid csv format at line " + lineNumber + " (expected 3 columns)");
        }

        String name = parts[0].trim();
        String email = parts[1].trim();
        String password = parts[2].trim();

        return new ImportedRow(name, email, password);
    }

    private String extractEmailHint(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length >= 2) {
            return parts[1].trim();
        }
        return "";
    }

    private record ImportedRow(String name, String email, String password) {
    }
}
