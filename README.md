# API de Biblioteca

API REST para gerenciar uma biblioteca: cadastro de livros e usuários, empréstimos, devoluções com multa por atraso e relatórios. Feita em Java com Spring Boot e PostgreSQL.

Fiz este projeto para praticar backend em Java, separando o código em camadas e colocando as regras de negócio no lugar certo.

## O que a API faz

- Cadastro completo de livros e de usuários (criar, listar, buscar, editar e excluir)
- Empréstimo de livros com controle de quantas cópias estão disponíveis
- Devolução com cálculo de multa por dia de atraso
- Relatórios: livros disponíveis, livros emprestados, usuários com atraso e ranking dos mais emprestados
- Validação dos dados recebidos e respostas de erro padronizadas

## Regras de negócio

- Um livro novo entra com todas as cópias disponíveis.
- Só é possível emprestar um livro que tenha cópia disponível. Cada empréstimo tira uma cópia e cada devolução coloca de volta.
- O prazo é de 14 dias para livros comuns e de 7 dias para livros raros.
- Devolução depois do prazo gera multa de R$ 1,00 por dia de atraso.
- Um empréstimo não devolvido e com o prazo vencido aparece com status `OVERDUE`.
- Não podem existir dois usuários com o mesmo e-mail.
- Não dá para excluir um livro ou um usuário que tenha empréstimos registrados.
- O total de cópias de um livro não pode ficar menor que a quantidade emprestada.

Os prazos e o valor da multa ficam no `application.properties` e podem ser alterados sem mexer no código.

## Tecnologias

- Java 21
- Spring Boot 4 (Web MVC, Data JPA, Validation)
- PostgreSQL
- Flyway para criar as tabelas
- Lombok
- JUnit e MockMvc para os testes, com banco H2 em memória

## Como rodar

Você precisa de Java 21 ou mais novo e de um PostgreSQL.

**1. Suba o banco.** Com Docker, basta um comando na pasta do projeto:

```bash
docker compose up -d
```

Se preferir usar um PostgreSQL já instalado, crie um banco chamado `library`.

**2. Rode a aplicação:**

```bash
./mvnw spring-boot:run
```

No Windows, use `mvnw.cmd spring-boot:run`.

A API sobe em `http://localhost:8080` e o Flyway cria as tabelas sozinho na primeira execução.

### Configuração

A conexão com o banco é lida de variáveis de ambiente. Se você não definir nenhuma, valem os valores padrão, que são os mesmos do `docker-compose.yml`.

| Variável | Padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/library` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` |

## Endpoints

### Livros

| Método | Rota | O que faz |
| --- | --- | --- |
| POST | `/books` | Cadastra um livro |
| GET | `/books` | Lista todos os livros |
| GET | `/books/{id}` | Busca um livro |
| PUT | `/books/{id}` | Atualiza os campos enviados |
| DELETE | `/books/{id}` | Exclui um livro |

### Usuários

| Método | Rota | O que faz |
| --- | --- | --- |
| POST | `/users` | Cadastra um usuário |
| GET | `/users` | Lista todos os usuários |
| GET | `/users/{id}` | Busca um usuário |
| PUT | `/users/{id}` | Atualiza os campos enviados |
| DELETE | `/users/{id}` | Exclui um usuário |

### Empréstimos

| Método | Rota | O que faz |
| --- | --- | --- |
| POST | `/loans` | Registra um empréstimo |
| PATCH | `/loans/{id}/return` | Registra a devolução |
| GET | `/loans` | Lista todos os empréstimos |
| GET | `/loans/active` | Lista os que ainda não foram devolvidos |
| GET | `/loans/user/{userId}` | Lista os empréstimos de um usuário |

### Relatórios

| Método | Rota | O que faz |
| --- | --- | --- |
| GET | `/reports/books/available` | Livros com cópia disponível |
| GET | `/reports/books/borrowed` | Livros com alguma cópia emprestada |
| GET | `/reports/users/overdue` | Usuários com empréstimo atrasado |
| GET | `/reports/books/ranking` | Livros do mais para o menos emprestado |

## Exemplos

Cadastrar um livro:

```bash
curl -X POST http://localhost:8080/books -H "Content-Type: application/json" -d '{"title": "Dom Casmurro", "author": "Machado de Assis", "category": "Romance", "publicationYear": 1899, "totalQuantity": 3, "rare": false}'
```

Cadastrar um usuário (o tipo pode ser `STUDENT`, `TEACHER` ou `VISITOR`):

```bash
curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name": "Ana Souza", "email": "ana@exemplo.com", "phone": "61 99999-0000", "userType": "STUDENT"}'
```

Emprestar o livro 1 para o usuário 1:

```bash
curl -X POST http://localhost:8080/loans -H "Content-Type: application/json" -d '{"userId": 1, "bookId": 1}'
```

Resposta:

```json
{
  "id": 1,
  "userId": 1,
  "bookId": 1,
  "loanDate": "2026-11-11",
  "dueDate": "2026-11-25",
  "returnDate": null,
  "status": "ACTIVE",
  "fineAmount": 0
}
```

Devolver:

```bash
curl -X PATCH http://localhost:8080/loans/1/return
```

### Erros

Todo erro volta no mesmo formato:

```json
{
  "timestamp": "2026-11-11T18:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Não existem cópias disponíveis deste livro.",
  "path": "/loans"
}
```

| Status | Quando acontece |
| --- | --- |
| 400 | Dados inválidos ou faltando |
| 404 | Livro, usuário ou empréstimo não encontrado |
| 409 | Alguma regra de negócio foi violada |

## Testes

```bash
./mvnw test
```

Os testes sobem a aplicação inteira com um banco H2 em memória e chamam os endpoints de verdade, então não precisam de PostgreSQL nem de Docker. Eles cobrem o fluxo de empréstimo e devolução, a multa por atraso, as validações e os relatórios.

## Estrutura do projeto

```
src/main/java/com/company/librarymanagement
├── controller   recebe as requisições HTTP
├── service      regras de negócio
├── repository   acesso ao banco com Spring Data JPA
├── model        entidades (Book, User, Loan)
├── dto          formato dos dados de entrada e de saída
├── exception    erros da aplicação e o tratamento global
├── config       CORS e propriedades da biblioteca
├── enums        status do empréstimo e tipo de usuário
└── util         limpeza de textos
```

As tabelas ficam em `src/main/resources/db/migration`.

## Próximos passos

- Autenticação e perfis de acesso
- Paginação nas listagens
- Documentação com Swagger
