# Sistema Cliente-Servidor em Java (Sockets + JSON)

Este projeto implementa uma aplicação cliente-servidor em Java que realiza operações como **cadastro**, **login**, **leitura de dados**, **edição de perfil**, **exclusão de conta** e **logout**. A comunicação entre cliente e servidor é feita via **sockets TCP** com mensagens no formato **JSON**.


---

## ⚙️ Tecnologias utilizadas

- Java 8 ou superior
- Biblioteca org.json (incluída no projeto)
- Sockets TCP
- Estrutura modular Cliente/Servidor

---

## 🚀 Funcionalidades

### No Cliente
- Cadastro de novo usuário
- Login com autenticação por token
- Leitura dos dados do usuário
- Edição do perfil (nome, usuário e senha)
- Exclusão da conta
- Logout

### No Servidor
- Validação e armazenamento de usuários
- Controle de sessões com tokens
- Persistência dos dados no arquivo `usuarios.json`

---

## 🖥️ Instalação e Execução

### Pré-requisitos

- [Java JDK 8+](https://www.oracle.com/java/technologies/javase-jdk8-downloads.html) instalado
- Terminal (CMD, Bash, etc.)

### 1. Baixe os arquivo .zip e extraia os arquivos
### 2. Compile os arquivos no terminal ou usando uma IDE
### 3. Ao iniciar o terminal, informe a porta. Ao iniciar o cliente, informe o ip do servidor e a mesma porta usada pelo servidor.


	
