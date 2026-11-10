package com.nttdata.users.adapter.in.web.error;

import com.nttdata.users.application.exception.BusinessValidationException;
import com.nttdata.users.application.exception.EmailAlreadyExistsException;
import com.nttdata.users.application.exception.UserNotFoundException;
import com.nttdata.users.adapter.in.web.dto.ImportInterruptedResponse;
import com.nttdata.users.adapter.in.web.dto.UserImportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(com.nttdata.users.application.exception.ImportInterruptedException.class)
    public ResponseEntity<Object> handleImportInterrupted(
            com.nttdata.users.application.exception.ImportInterruptedException ex) {
        return ResponseEntity.status(ex.getCodigo()).body(
                new ImportInterruptedResponse(ex.getMessage(), UserImportResponse.from(ex.getParcial())));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleDuplicateEmail(EmailAlreadyExistsException ex) {
        return response(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(UserNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Object> handleBusinessValidation(BusinessValidationException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIntegrityConflict(DataIntegrityViolationException ex) {
        return response(HttpStatus.CONFLICT,
                "A operação viola uma restrição dos dados.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        return response(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // Inclui erros de campo e de objeto, sem expor os valores rejeitados.
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage() == null
                        ? "Valor inválido." : error.getDefaultMessage())
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));
        return new ResponseEntity<>(new ErrorResponse(message), headers, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // Padroniza também JSON inválido, rota inexistente, método não permitido etc.
        // Preserva os headers definidos pelo Spring, como Allow no HTTP 405.
        String message = switch (status.value()) {
            case 400 -> "A requisição contém dados ausentes, malformados ou incompatíveis.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não permitido para este recurso.";
            case 406 -> "O formato de resposta solicitado não é suportado.";
            case 413 -> "O arquivo enviado excede o tamanho permitido.";
            case 415 -> "O tipo de conteúdo enviado não é suportado.";
            default -> status.is5xxServerError()
                    ? "Não foi possível concluir a operação." : "A requisição não pôde ser atendida.";
        };
        if (status.is5xxServerError()) {
            LOG.error("Falha interna do tipo {}", ex.getClass().getName());
        }
        return super.handleExceptionInternal(ex,
                new ErrorResponse(message),
                headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        // Nao devolvemos nem registramos mensagens técnicas que possam conter segredos.
        LOG.error("Falha inesperada do tipo {}", ex.getClass().getName());
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "Não foi possível concluir a operação.");
    }

    private ResponseEntity<Object> response(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(message));
    }
}
