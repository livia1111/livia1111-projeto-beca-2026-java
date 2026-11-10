package com.nttdata.users.adapter.in.web.error;

public final class SwaggerExamples {
    private SwaggerExamples() { }

    public static final String VALIDATION = "{\"mensagem\":\"Informe um e-mail válido.\"}";
    public static final String UNAUTHORIZED = "{\"mensagem\":\"Credenciais HTTP Basic ausentes ou inválidas.\"}";
    public static final String FORBIDDEN = "{\"mensagem\":\"Você não tem permissão para acessar este recurso.\"}";
    public static final String EMAIL_CONFLICT = "{\"mensagem\":\"Já existe um usuário cadastrado com este e-mail.\"}";
    public static final String NOT_FOUND = "{\"mensagem\":\"Usuário não encontrado.\"}";
    public static final String IMPORT_REPORT = """
            {"cadastrados":1,"rejeitados":1,"resultados":[
              {"linha":2,"email":"bia@example.com","status":"CADASTRADO","codigo":201,"mensagem":"Usuário cadastrado com sucesso."},
              {"linha":3,"email":"caio@example.com","status":"REJEITADO","codigo":409,"mensagem":"Já existe um usuário cadastrado com este e-mail."}
            ]}
            """;
    public static final String PARTIAL_IMPORT = """
            {"mensagem":"Importação interrompida. Consulte o relatório antes de reenviar o arquivo.",
             "parcial":{"cadastrados":1,"rejeitados":0,"resultados":[
               {"linha":2,"email":"bia@example.com","status":"CADASTRADO","codigo":201,"mensagem":"Usuário cadastrado com sucesso."}
             ]}}
            """;
}
