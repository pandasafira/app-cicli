
# Cicli

**Cicli** é um aplicativo Android desenvolvido para auxiliar mulheres na rotina diária do uso de anticoncepcionais orais, oferecendo lembretes personalizados, acompanhamento das doses e acesso a informações sobre medicamentos.

O alarme permanece ativo até que a usuária informe se tomou ou não o comprimido. Caso não haja resposta, ele é repetido automaticamente, respeitando o intervalo e o limite de repetições configurados.

Projeto desenvolvido para a disciplina **Projeto Tecnológico em Desenvolvimento de Sistemas**, do curso de Análise e Desenvolvimento de Sistemas — ULBRA EAD.

**Autora:** Camile Kaghofer Schicht

---

## Funcionalidades

- **Cadastro inicial:** nome, data de nascimento, anticoncepcional e tipo de cartela, sem necessidade de login ou senha.
- **Alarme personalizado:** configuração de horário, intervalo, limite de repetições e escolha da música.
- **Funcionamento em segundo plano:** alarmes que funcionam com a tela bloqueada e são reagendados após a reinicialização do aparelho.
- **Confirmação de doses:** opções "Tomei" e "Não tomei" diretamente na tela do alarme.
- **Controle de pausas:** suspensão automática dos alarmes durante os dias de pausa da cartela.
- **Gerenciamento de cartelas:** possibilidade de trocar a cartela sem perder o histórico anterior.
- **Histórico de doses:** acompanhamento dos registros de doses tomadas e não tomadas nos últimos três meses.
- **Informações sobre medicamentos:** busca de anticoncepcionais, princípios ativos, tolerância de atraso e medicamentos que podem reduzir sua eficácia.
- **Funcionamento offline:** os dados são armazenados localmente no dispositivo, sem necessidade de conexão com a internet.

> O Cicli tem finalidade informativa e de organização da rotina. Não realiza diagnósticos, não prescreve medicamentos e não recomenda alterações no método contraceptivo.

---

## Tecnologias utilizadas

| Tecnologia | Versão |
|---|---|
| Kotlin | 2.0.21 |
| Android Studio | JDK 25 |
| Android Gradle Plugin | 8.13.2 |
| Gradle Wrapper | 9.5.0 |
| KSP | 2.0.21-1.0.27 |
| Room (SQLite) | 2.6.1 |
| Material Components | 1.12.0 |
| AppCompat | 1.7.0 |
| Android mínimo | API 24 (Android 7.0) |
| Android alvo | API 35 |
| Tratamento dos dados | Python, gspread (leitura da planilha com os dados da ANVISA) e sqlite3 (carga no banco) |

**Fonte dos dados:** informações sobre medicamentos obtidas da ANVISA e processadas com Python antes de serem incorporadas ao banco de dados do aplicativo.

---

## Instalação do aplicativo

1. Baixe o APK pelo seguinte link: **[Download do Cicli](https://drive.google.com/file/d/1h5VtX3UocCVoWRGlnc6VBxb58kKHBuoD/view?usp=sharing)**.
2. Abra o arquivo baixado e selecione **Instalar**.
3. No celular, autorize a instalação de aplicativos de fontes desconhecidas.
   
  **3.1. Como autorizar a instalação?**
   
  Em versões mais recentes do Android, pode ser necessário desativar temporariamente o **Bloqueador Automático** para permitir a instalação do aplicativo.
  
  - Acesse **Configurações > Segurança e privacidade**.
  - Localize a opção **Bloqueador Automático** e desative-a temporariamente.
  
  Se não solicitar a desativação do bloqueador automático, siga com os passos abaixo:
  
  1. Durante a instalação, o Google Play Protect poderá exibir um aviso de segurança informando que esse aplicativo é nocivo.
  2. Caso apareça o aviso, selecione **Mais detalhes** e, em seguida, **Instalar mesmo assim**.

Após essas etapas, a instalação deverá prosseguir normalmente.

**Importante:** Nenhum tipo de malware foi implementado na aplicação.

4. Ao iniciar o aplicativo, conceda as permissões solicitadas, especialmente notificações, alarmes em tela cheia e configurações necessárias para o funcionamento em segundo plano.

**Observação:** a concessão das permissões necessárias é importante para garantir o funcionamento adequado dos alarmes.

## Instalação no emulador do Android Studio

  1. Abra o Android Studio.
  2. Inicie seu emulador pelo Device Manager.
  3. Aguarde o Android iniciar completamente.
  4. Localize o arquivo .apk no seu computador.
  5. Arraste o APK para dentro da tela do emulador.
  6. Aguarde a instalação e procure o Cicli na lista de aplicativos.

---

## Como executar o projeto

### Pré-requisitos

- Android Studio em versão compatível com o projeto.
- JDK 25.
- Emulador Android ou dispositivo físico com Android 7.0 ou superior.

### Passo a passo

1. Clone este repositório:

   ```bash
   git clone https://github.com/pandasafira/app-cicli.git
   ```

2. Abra a pasta do projeto no Android Studio.
3. Aguarde a sincronização do Gradle.
4. Selecione um emulador ou conecte um dispositivo Android.
5. Clique em **Run** para compilar e executar o aplicativo.

No primeiro acesso, o aplicativo apresenta a tela de cadastro inicial. Não é necessário criar uma conta ou utilizar senha.

### Testando o alarme

Para testar o funcionamento sem precisar aguardar o horário habitual:

1. Configure um alarme para dois minutos à frente.
2. Defina o intervalo de repetição como um minuto.
3. Configure duas repetições.
4. Aguarde o disparo e teste as opções de confirmação.

---

## Banco de dados

O banco é SQLite, gerenciado pelo Room. O arquivo `cicli.db` já vem pronto e fica em `app/src/main/assets/`. Uma cópia está na pasta `Banco_de_dados/`.

Ele já vem com os dados dos anticoncepcionais (fonte ANVISA): pílulas, princípios ativos e medicamentos a evitar. As tabelas da usuária (users, pill_pack, alarm e medication_history) começam vazias de propósito, por causa da LGPD.

### Arquivos da pasta `Banco_de_dados/`

| Arquivo | Para que serve |
|---|---|
| cicli.db | Banco pronto, com os dados iniciais |
| cicli_criacao.sql | Script de criação das tabelas |
| tratamento_dados.py | Script que tratou os dados da planilha e carregou no banco |
| consultas_exemplo.sql | Consultas de exemplo. A Parte 3 mostra as tabelas da usuária preenchidas |

### Tratamento dos dados (Python)

O `tratamento_dados.py` lê a planilha com os dados da ANVISA, padroniza os nomes e grava no banco. É um script de carga única, e o `cicli.db` já foi gerado com ele. Não é preciso rodar para usar o app.

As partes das pílulas e dos princípios ativos estão comentadas porque já foram executadas. Rodar de novo duplicaria linhas de `medicine_to_avoid`.

Se quiser rodar mesmo assim, use uma cópia do banco:

1. Instale as dependências: `pip install gspread python-dotenv`
2. Adicione e preencha o caminho da credencial do Google em um arquivo `.env`. Exemplo: google_cred=caminho/para/credenciais.json
3. Rode: `python tratamento_dados.py`

A credencial do Google e o `.env` não estão no repositório, por segurança.

### Ver as tabelas da usuária preenchidas

Abra uma cópia do `cicli.db` no DBeaver e rode a Parte 3 do `consultas_exemplo.sql`.

---

## Guia de análise do código

O código-fonte principal está localizado em:

`app/src/main/java/com/example/aplicativocicli/`

### Telas e funcionalidades

| Arquivo ou pasta | Responsabilidade |
|---|---|
| `cadastro.kt` | Cadastro inicial e validação de nome e datas. |
| `main.kt` | Tela inicial, horário do alarme, fase da cartela, confirmação de dose, busca e menu. |
| `alarme.kt` | Criação e edição de alarmes. |
| `config_alarme.kt` | Gerenciamento dos alarmes: ativar, editar e excluir. |
| `alterar_cartela.kt` | Troca de cartela. |
| `historicoAlarme.kt` | Histórico de doses e exclusão de registros com mais de três meses. |
| `infos_medicamentos.kt` | Exibição das informações dos medicamentos. |
| `classes/` | DAOs responsáveis pelas consultas ao banco. |
| `data/entidades.kt` | Definição das entidades do banco de dados. |
| `data/database.kt` | Configuração do banco de dados Room. |

### Sistema de alarmes

Os arquivos responsáveis pelo funcionamento dos alarmes estão organizados na pasta `alarm/`.

| Arquivo | Responsabilidade |
|---|---|
| `alarme_agenda.kt` | Agendamento dos alarmes e das repetições utilizando AlarmManager. |
| `AlarmReceiver.kt` | Recebe o disparo do alarme e verifica se o dia corresponde a uma pausa da cartela. |
| `AlarmTimeoutReceiver.kt` | Trata alarmes sem resposta, realizando novas repetições ou registrando a dose como não tomada. |
| `TelaAlarmeAtividade.kt` | Exibe a tela do alarme e as opções de confirmação. |
| `notificacao.kt` | Gerenciamento das notificações em tela cheia. |
| `som.kt` | Reprodução da música selecionada para o alarme. |
| `dia_pausa.kt` | Identificação dos dias de pausa da cartela. |
| `ajuste_celular_desligado.kt` | Reagendamento dos alarmes após a reinicialização do dispositivo. |

### Fluxo de funcionamento do alarme

1. `alarme_agenda` agenda o disparo utilizando o AlarmManager.
2. `AlarmReceiver` recebe o evento e verifica se o dia atual é de pausa.
3. `notificacao` apresenta a tela do alarme e `som` reproduz a música configurada.
4. A usuária seleciona "Tomei" ou "Não tomei". Caso não responda, `AlarmTimeoutReceiver` gerencia as repetições.
5. A situação da dose é registrada na tabela `medication_history`.

---

## Considerações finais

O Cicli foi desenvolvido com o objetivo de unir tecnologia, organização e informação em uma ferramenta acessível e simples de utilizar.

O projeto também permitiu aplicar conhecimentos de desenvolvimento Android, programação Kotlin, banco de dados SQLite, persistência local e gerenciamento de notificações e alarmes.
