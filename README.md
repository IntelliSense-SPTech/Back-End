# Back-End: Serviço de Tratamento e Armazenamento de Dados

## Descrição
Este repositório contém o back-end do projeto de um serviço para consumo, tratamento e armazenamento de dados. O objetivo é processar dados provenientes do S3, armazená-los em um banco de dados, e enviar notificações via e-mail e Slack com anexos e relatórios gerados.

O back-end integra diversas funções, incluindo leitura de dados, tratamento e armazenamento eficiente, além da automação de notificações para suporte às decisões de negócio.

## Funcionalidades
- **Processamento de Dados**: Consome arquivos armazenados no S3, realiza o tratamento e insere os dados no banco de dados.
- **Manipulação de Arquivos Excel**: Leitura e extração de dados de planilhas utilizando a biblioteca Apache POI.
- **Notificações Automatizadas**: Envia mensagens e relatórios via e-mail e API do Slack.
- **Conexão com Banco de Dados**: Insere os dados tratados em um banco de alta performance.
- **Operações com AWS S3**: Download, upload e manipulação de arquivos no bucket S3.

## Tecnologias Utilizadas
- **Java**: Linguagem de programação principal.
- **Log4j**: Gerenciamento de logs e monitoramento.
- **AWS SDK**: Integração com serviços AWS (S3, entre outros).
- **MySQL**: Banco de dados para armazenamento das informações processadas.
- **Maven**: Gerenciamento de dependências e build.
- **API do Slack**: Envio de mensagens automatizadas para canais e usuários.
- **Bibliotecas para E-mail**: Envio de notificações personalizadas por e-mail.

## Como Executar o Projeto
1. **Clone o repositório**:
   ```bash
   git clone https://github.com/IntelliSense-SPTech/Back-End.git
   ```
2. **Acesse a pasta do projeto**:
   ```bash
   cd Back-End
   ```
   
3. **Compile o projeto**:
   ```bash
   mvn clean install
   ```
4. **Execute o projeto**:
   ```bash
   java -jar target/Back-End-1.0.jar
   ```
   
## Contato
Para dúvidas ou sugestões, entre em contato pelo e-mail: **intellisense-sptech@outlook.com**.

## Integrantes:
- Giovanna Gomes: [GitHub](https://github.com/giovannagomeslm) [LinkedIn](https://www.linkedin.com/in/giovannagomes1)
- Jhenifer Lorrane: [GitHub](https://github.com/jheniferlorrane) [LinkedIn](https://www.linkedin.com/in/jheniferanacleto)

## Estrutura do Repositório
```bash
📦 Back-End            
├── 📁 src
│   ├── 📁 main
│   │   ├── 📁 java
│   │   │   └── 📁 notificacao
│   │   │       ├── Email.java       # Classe para envio de e-mails
│   │   │       └── Slack.java       # Classe para envio de mensagens no Slack
│   │   │   ├── BancoDados.java  # Lógica de inserção no banco de dados
│   │   │   ├── Crime.java       # Modelo de dados para crimes
│   │   │   ├── DBConnectionProvider.java # Provedor de conexão ao BD
│   │   │   ├── Leitor.java      # Classe para leitura de arquivos
│   │   │   ├── OperacoesBucket.java # Manipulação de arquivos no S3
│   │   │   ├── S3Provider.java  # Configuração do cliente S3
│   │   │   └── SistemaIntelliSense.java # Classe principal do sistema
│   │   └── 📁 resources
│   │       ├── log4j.properties     # Configuração de logs
├── 📁 target               # Arquivos gerados após o build
├── .gitignore              # Arquivos ignorados pelo Git
├── pom.xml                 # Gerenciamento de dependências Maven
└── README.md               # Documentação do projeto
```
