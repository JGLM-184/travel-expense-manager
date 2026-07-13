# Travel Expense Manager API

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.15-6DB33F?style=for-the-badge\&logo=springboot\&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge\&logo=springsecurity\&logoColor=white)
![OAuth2 Resource Server](https://img.shields.io/badge/OAuth2-Resource_Server-4285F4?style=for-the-badge\&logo=oauth\&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-RSA-000000?style=for-the-badge\&logo=jsonwebtokens)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge\&logo=postgresql\&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge\&logo=docker\&logoColor=white)
![JUnit 5](https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge\&logo=junit5\&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge\&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-3-6BA539?style=for-the-badge\&logo=swagger\&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge\&logo=prometheus\&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge\&logo=grafana\&logoColor=white)

---

## Sobre o Projeto

O **Travel Expense Manager API** é uma API REST desenvolvida em **Java** e **Spring Boot** para gerenciamento de despesas de viagens corporativas.

O projeto foi construído seguindo boas práticas de desenvolvimento backend, priorizando segurança, organização do código, escalabilidade e observabilidade. Além do gerenciamento de usuários, viagens e despesas, a aplicação oferece autenticação baseada em **OAuth2 Resource Server** com **JWT assinado por chaves RSA**, documentação automática da API e monitoramento completo da aplicação através do ecossistema **Spring Boot Actuator + Micrometer + Prometheus + Grafana**.

A arquitetura também implementa **multitenancy**, permitindo que múltiplas empresas utilizem a mesma aplicação de forma totalmente isolada e segura.

---

# Funcionalidades

* Cadastro e autenticação de usuários
* Controle de acesso baseado em papéis
* Gerenciamento de empresas
* Gerenciamento de viagens corporativas
* Cadastro e controle de despesas
* Integração com ViaCEP para preenchimento automático de endereços
* Integração com ReceitaWS para consulta de dados de empresas
* Documentação automática com OpenAPI/Swagger
* Monitoramento de métricas da aplicação
* Arquitetura Multitenant
* Persistência em PostgreSQL
* Ambiente totalmente containerizado com Docker Compose

---

# Tecnologias Utilizadas

## Backend

* Java 21
* Spring Boot 3.5.15
* Spring Security
* OAuth2 Resource Server
* Spring Data JPA
* Hibernate
* PostgreSQL

## Testes

* JUnit 5
* Mockito
* TestRestTemplate

## Documentação

* SpringDoc OpenAPI (Swagger UI)

## Observabilidade

* Spring Boot Actuator
* Micrometer
* Prometheus
* Grafana

## Infraestrutura

* Docker
* Docker Compose

## APIs Externas

* ViaCEP
* ReceitaWS

---

# Segurança

A autenticação é realizada utilizando **OAuth2 Resource Server** com **JSON Web Tokens (JWT)** assinados por **chaves RSA**.

O fluxo de autenticação utiliza:

* Chave privada para assinatura dos tokens durante o login.
* Chave pública para validação da autenticidade dos tokens em todas as requisições protegidas.

As chaves criptográficas **não fazem parte do código-fonte** e devem ser fornecidas através de variáveis de ambiente, seguindo boas práticas de segurança.

Além da autenticação, o sistema utiliza **controle de acesso baseado em papéis (RBAC)**, definindo permissões específicas para cada perfil:

| Perfil       | Permissões                                               |
| ------------ | -------------------------------------------------------- |
| **ADMIN**    | Administração completa da plataforma                     |
| **MANAGER**  | Gerenciamento de viagens e despesas de sua empresa       |
| **EMPLOYEE** | Cadastro e consulta das próprias viagens e despesas      |  

---

# Arquitetura Multitenant

A aplicação utiliza uma arquitetura **Shared Database / Shared Schema** com isolamento lógico por empresa.

Cada usuário pertence a um único **tenant**, e todas as operações realizadas na API são automaticamente limitadas aos dados pertencentes à empresa autenticada.

Esse modelo garante:

* isolamento completo entre empresas;
* segurança no acesso aos dados;
* compartilhamento da mesma infraestrutura;
* escalabilidade para múltiplos clientes.

---

# Como Executar o Projeto

## Pré-requisitos

* Docker
* Docker Compose
* Git
* OpenSSL (para geração das chaves RSA)
* Cliente HTTP (Postman, Insomnia ou REST Client)

---

## 1. Clonar o repositório

```bash
git clone https://github.com/JGLM-184/travel-expense-manager.git

cd travel-expense-manager
```

---

## 2. Gerar as chaves RSA

Gere uma chave privada RSA de 2048 bits:

```bash
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
```

Extraia a chave pública:

```bash
openssl rsa -pubout -in private.pem -out public.pem
```

Converta ambas para Base64 em uma única linha e utilize seus valores nas variáveis de ambiente da aplicação.

---

## 3. Configurar as variáveis de ambiente

Crie um arquivo `.env` utilizando o arquivo `.env.example` como referência e preencha as informações necessárias.

---

## 4. Iniciar a aplicação

Na raiz do projeto execute:

```bash
docker compose up -d --build
```

Esse comando irá:

* construir a aplicação;
* iniciar o banco PostgreSQL;
* iniciar a API;
* iniciar o Prometheus;
* iniciar o Grafana.

---

# Serviços Disponíveis

| Serviço    | URL                                   |
| ---------- | ------------------------------------- |
| API        | http://localhost:8080                 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Prometheus | http://localhost:9090                 |
| Grafana    | http://localhost:3000                 |

Credenciais iniciais do Grafana:

```
Usuário: admin
Senha: admin
```

---

# Observabilidade

A aplicação possui monitoramento integrado utilizando o ecossistema Spring.

As métricas são disponibilizadas pelo **Spring Boot Actuator**, coletadas pelo **Prometheus** e exibidas em dashboards do **Grafana**, permitindo acompanhar informações como:

* utilização de memória JVM;
* consumo de CPU;
* utilização de threads;
* métricas HTTP;
* tempo de resposta;
* conexões com banco de dados;
* status da aplicação;
* health checks.

Os dashboards são carregados automaticamente através do mecanismo de provisioning do Grafana.

---

# Executando os Testes

O projeto possui duas suítes de testes organizadas por **Maven Profiles**.

## Testes Unitários

Executa todos os testes cujo nome termina com `*Test.java`.

```bash
./mvnw test -Punit-tests
```

## Testes de Integração

Executa todos os testes cujo nome termina com `*IT.java`.

```bash
./mvnw test -Pintegration-tests
```

Os testes validam:

* regras de negócio da aplicação;
* camada de serviços;
* autenticação e autorização;
* integração entre os componentes da API;
* comportamento dos endpoints REST.


---

# Encerrando a Aplicação

Parar os containers mantendo os dados persistidos:

```bash
docker compose down
```

Parar os containers removendo também os volumes do banco de dados:

```bash
docker compose down -v
```
