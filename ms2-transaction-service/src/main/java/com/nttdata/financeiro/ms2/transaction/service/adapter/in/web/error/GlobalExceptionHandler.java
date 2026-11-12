package com.nttdata.financeiro.ms2.transaction.service.adapter.in.web.error;

import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccessDeniedToTransactionException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountServiceUnavailableException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.AccountNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.BusinessValidationException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.InvalidCredentialsException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TargetUserNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.TransactionNotFoundException;
import com.nttdata.financeiro.ms2.transaction.service.application.exception.UserServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
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

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"ms2\", charset=\"UTF-8\"")
                .body(new ErrorResponse("Credenciais HTTP Basic ausentes ou inválidas."));
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<Object> handleUserServiceUnavailable(UserServiceUnavailableException ex) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(AccountServiceUnavailableException.class)
    public ResponseEntity<Object> handleAccountServiceUnavailable(AccountServiceUnavailableException ex) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFound(AccountNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<Object> handleTransactionNotFound(TransactionNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TargetUserNotFoundException.class)
    public ResponseEntity<Object> handleTargetUserNotFound(TargetUserNotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Object> handleBusinessValidation(BusinessValidationException ex) {
        return response(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedToTransactionException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedToTransactionException ex) {
        return response(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar esta transação.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleIntegrityConflict(DataIntegrityViolationException ex) {
        return response(HttpStatus.CONFLICT, "A operação viola uma restrição dos dados.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
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
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String message = switch (status.value()) {
            case 400 -> "A requisição contém dados ausentes, malformados ou incompatíveis.";
            case 404 -> "Recurso não encontrado.";
            case 405 -> "Método HTTP não permitido para este recurso.";
            case 406 -> "O formato de resposta solicitado não é suportado.";
            case 413 -> "O conteúdo enviado excede o tamanho permitido.";
            case 415 -> "O tipo de conteúdo enviado não é suportado.";
            default -> status.is5xxServerError()
                    ? "Não foi possível concluir a operação."
                    : "A requisição não pôde ser atendida.";
        };

        if (status.is5xxServerError()) {
            LOG.error("Falha interna do tipo {}", ex.getClass().getName());
        }

        return super.handleExceptionInternal(
                ex,
                new ErrorResponse(message),
                headers,
                status,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex) {
        LOG.error("Falha inesperada do tipo {}", ex.getClass().getName());
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível concluir a operação.");
    }

    private ResponseEntity<Object> response(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message));
    }
}