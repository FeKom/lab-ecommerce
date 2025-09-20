# Microserviço de Catálogo e Busca para E-commerce

## Descrição

Este projeto é um microserviço responsável pelo gerenciamento do catálogo de produtos e funcionalidades de busca em uma plataforma de e-commerce. Ele foi projetado com base nos princípios de Domain-Driven Design (DDD) e Clean Architecture, garantindo uma separação clara de responsabilidades, alta coesão e baixo acoplamento. A aplicação é escalável, eficiente e integrada com tecnologias modernas para suportar alta disponibilidade e desempenho.

## Tecnologias Utilizadas

- Java: Linguagem principal para desenvolvimento do microserviço.


- Spring: Framework utilizado para construção da aplicação (Spring Boot, Spring Data, Spring Web, etc.).


- MongoDB: Banco de dados NoSQL para armazenamento de dados flexíveis do catálogo de produtos.


- PostgreSQL: Banco de dados relacional para dados estruturados, como metadados de produtos.


- Redis: Utilizado como cache para otimizar consultas frequentes e melhorar a performance de busca.


- Kafka: Sistema de mensageria para comunicação assíncrona entre microserviços (ex.: eventos de atualização de catálogo).


- Testcontainers: Biblioteca para testes de integração com contêineres Docker, garantindo testes consistentes e isolados.

### Arquitetura

O microserviço segue os princípios de Domain-Driven Design (DDD) e Clean Architecture, estruturado em camadas para promover manutenção escalabilidade e desacoplamento. A arquitetura é orientada a eventos e utiliza os seguintes componentes:


* Domínio (DDD): O núcleo da aplicação é modelado com base em DDD, com agregados, entidades, objetos de valor e serviços de domínio que representam as regras de negócio do catálogo e busca. O domínio é isolado de detalhes técnicos, garantindo que a lógica de negócio permaneça independente.


* Clean Architecture: A aplicação é dividida em camadas (Domínio, Aplicação, Infraestrutura e Interface), com dependências apontando para dentro, seguindo o princípio de inversão de dependência. Isso facilita a testabilidade e a substituição de componentes (ex.: troca de banco de dados).


* Catálogo: CRUD de produtos, armazenados no MongoDB para flexibilidade, Redis com TL de 15 Min para o kafka, com agregados definidos segundo DDD.


* Busca: Funcionalidades de pesquisa otimizadas com Redis para cache de resultados frequentes, PostgreSQL para relacionamento de produtos, implementadas como um serviço de aplicação.


* Eventos: Integração com Kafka para publicar e consumir eventos relacionados a alterações no catálogo


* Persistência Híbrida: Combinação de MongoDB (NoSQL) para dados não estruturados e PostgreSQL (SQL) para dados relacionais, acessados via repositórios na camada de infraestrutura.


* Testes: Testcontainers é usado para criar ambientes de teste, garantindo que os testes de integração respeitem a estrutura de camadas e os contratos do domínio.

## Pré-requisitos


1. Java 17 ou superior


2. Maven 3.8.x


3. Docker (para Testcontainers e execução de serviços como Kafka, Redis, MongoDB e PostgreSQL)


### Instalação

1. Clone o repositório:

```bash
  git clone https://github.com/fekom/lab-ecommerce.git
  cd lab-ecommerce
```

2. Crie os Containers
```   bash
   docker-compose --build up -d
```
3. Compile e execute o projeto
```   bash
   mvn clean install
   mvn spring-boot:run
```

### Testes

Os testes são executados com Testcontainers para simular serviços externos (MongoDB, PostgreSQL, Redis, Kafka) em contêineres Docker. A estrutura de testes respeita os princípios de Clean Architecture, com testes unitários focados no domínio e testes de integração validadando as camadas de infraestrutura e aplicação.

1. Execute os testes:
```bash
   mvn test
```
## Escalabilidade





- Redis: Cacheia resultados de busca para reduzir a latência, implementado na camada de infraestrutura.



- Kafka: Permite integração assíncrona com outros microserviços, com eventos de domínio bem definidos.



- MongoDB/PostgreSQL: Combinação de bancos para balancear flexibilidade e consistência, acessados via repositórios que isolam a lógica de persistência do domínio.



- DDD e Clean Architecture: A separação clara de camadas e a modelagem orientada ao domínio facilitam a evolução e manutenção do sistema em larga escala.