# API Users - MS1

Base incremental com Java 25, Spring Boot 4.1.2 e PostgreSQL.

## Executar com Docker

Requisito: Docker Desktop iniciado, usando containers Linux e Docker Compose.
O build usa Maven e Java 25 no container; nao exige instalacao local dessas ferramentas.

1. Se `.env` ainda nao existir, copie `.env.example` para `.env`.
2. Preencha `POSTGRES_PASSWORD` com uma senha local. Nao versione `.env`.
3. Execute na raiz:

```powershell
docker compose up --build -d
docker compose logs -f app
```

Depois que a aplicacao iniciar:

```powershell
Invoke-RestMethod http://localhost:8080/status
```

Resposta esperada:

```json
{"application":"ms1","status":"UP"}
```

Esse endpoint verifica somente a resposta HTTP, nao a saude do banco.

```powershell
docker compose ps
docker compose down
```

O volume preserva o banco ao executar `down`. A senha de inicializacao do
PostgreSQL so e aplicada quando o volume esta vazio; mudar o `.env` nao altera
a senha de um banco existente.

O banco fica acessivel apenas na rede do Compose. A API e publicada em
127.0.0.1, na porta APP_PORT (8080 por padrao).

## Administrador inicial local

As migrations V2 e V3 provisionam o usuario `admin@admin.com`, com senha inicial
`admin` e perfil `ADMIN` em qualquer banco novo. A senha e armazenada como hash
BCrypt. A V3 também corrige o e-mail nos bancos que já executaram a V2 antiga
com o erro de digitação `admim@admin.com`, sem alterar o checksum da V2.

A API usa HTTP Basic: informe o e-mail como usuario e a senha no Swagger
(botao **Authorize**) ou no cabeçalho `Authorization: Basic ...`. O endpoint
`POST /auth/login` valida essas credenciais e devolve o UUID fixo do usuario;
as demais chamadas continuam enviando Basic. `POST /users`, `POST /users/import`
e `GET /users` exigem perfil `ADMIN`. Em `GET /users/{id}`, usuários autenticados
consultam apenas o próprio perfil; ADMIN pode consultar qualquer usuário. Para
`PUT` e `DELETE`, o próprio usuário ou um ADMIN pode operar sobre a conta indicada.

O PUT /users/{id} recebe nome, email e senha; o perfil existente não pode
ser alterado nessa operação.

## Estado deste passo

- Porta PasswordHasher e adaptador BCrypt registrados explicitamente por configuracao.
- Controller GET /status registrado sem component scanning.
- Hibernate configurado para validar, sem criar tabelas.
- Flyway cria a tabela users pela migration V1__create_users.sql.
- User e Role sao modelos Java puros; UserJpaEntity contem o mapeamento JPA.
- O banco exige email normalizado e unico, role valida e datas obrigatorias.
- User.criar gera o UUID e preenche as duas datas com Instant.now(clock).
- User.reconstituir preserva o UUID e as datas recebidos do banco.
- O caso de uso usa Clock e o hash produzido pela porta PasswordHasher.
- Contrato de cadastro: CreateUserRequest -> CreateUserCommand -> CreateUserUseCase.
- UserWebMapper converte a entrada e cria UserResponse sem senha ou hash.
- DTO de entrada possui Bean Validation, acionada por @Valid no controller.
- O limite de senha em bytes UTF-8 exigido pelo BCrypt e validado no caso de uso.
- CRUD implementado; HTTP Basic protege as rotas e ADMIN controla cadastro/importacao.
- GlobalExceptionHandler padroniza erros HTTP e validacao, registrado por @Bean.
- EmailAlreadyExistsException gera 409; UserNotFoundException gera 404.
- UserPersistenceExceptionTranslator traduz conflitos de unicidade do banco para erro de negocio.
- O JSON de erro contem apenas mensagem; o status fica na resposta HTTP.
- As excecoes de negocio definem suas mensagens com super(...).
- Falhas inesperadas retornam 500 sem detalhes internos; o log registra apenas
  o tipo da excecao, sem mensagens tecnicas ou valores sensiveis.

O Dockerfile usa build em duas etapas, cache Maven e runtime com usuario sem
privilegios. O empacotamento executa os testes; uma falha impede gerar a imagem.
Arquivos de ambiente ficam fora do Git e do contexto de build.

Os testes do handler usam MockMvc sem banco e cobrem validacao, conflitos,
falhas inesperadas e erros HTTP. As rotas /test existem somente nesses testes.

## Swagger e importação CSV

Abra http://localhost:8080/swagger-ui.html (ajuste a porta se mudou APP_PORT).
O contrato OpenAPI está em http://localhost:8080/v3/api-docs.
Envie o arquivo de texto CSV diretamente no corpo de `POST /users/import`, com
`Content-Type: text/plain` ou `text/csv`, autenticado como ADMIN.

O arquivo deve usar UTF-8 e este cabeçalho, nesta ordem:

```csv
nome;email;role;senha
Ana;ana@example.com;USER;SenhaTeste123
```

O único separador aceito é ponto e vírgula. Campos com ponto e vírgula podem usar aspas duplas; aspas dentro
de um campo são escapadas como duas aspas. Cada usuário deve ocupar uma linha
física: campos com quebras de linha não são suportados. Linhas vazias são rejeitadas.
O perfil deve ser ADMIN ou USER. São aceitos até 1 MB e 1.000 linhas de dados.

O processamento recebe um InputStream e lê uma linha por vez, sem carregar o
CSV inteiro em memória. O relatório fica em memória, limitado a 1.000 resultados.
A resposta HTTP 200 significa que o arquivo foi processado; consulte o resultado
de CADA linha. O campo codigo vale 201 para cadastrado, 400 para dados inválidos
e 409 para e-mail duplicado. O e-mail é normalizado com strip e toLowerCase.
Não há senha, hash ou conteúdo bruto da linha no relatório. O e-mail é incluído
nas linhas rejeitadas quando foi possível extraí-lo; em caso de CSV ilegível,
use o número da linha para identificá-la.

Um erro de dados não impede o cadastro das próximas linhas. Cada gravação tem
sua própria transação. Cabeçalho inválido gera HTTP 400 sem inserir usuários.
Ao exceder 1.000 linhas, a API retorna HTTP 400 com relatório parcial e mantém
os cadastros anteriores. Falhas de leitura/infraestrutura retornam HTTP 500 com
relatório parcial; uma confirmação perdida pode exigir consulta antes do reenvio.
Arquivo maior que o limite retorna HTTP 413. Não existe rollback do lote inteiro.
Reenviar um arquivo já importado produz rejeições 409 para os e-mails existentes.

### Fluxo hexagonal

```text
UserController (HTTP /users/import, corpo em stream; perfil ADMIN)
  -> ImportUsersUseCase (porta de entrada)
    -> ImportUsersService
      -> UserCsvReader (porta) -> UserCsvReaderAdapter (CSV + Bean Validation)
      -> CreateUserUseCase -> CreateUserService
        -> PasswordHasher -> BCryptPasswordHasherAdapter
        -> UserRepository -> UserPersistenceAdapter -> PostgreSQL
```

As instâncias são criadas explicitamente nas classes de configuração.
O adaptador CSV usa um modelo de validação próprio (`CsvUserInput`), sem depender
dos DTOs do adaptador HTTP. O serviço de importação não depende de Spring,
MultipartFile, Apache Commons CSV nem Jakarta Validation.


