# Changelog

Todas as mudanças relevantes deste projeto são documentadas aqui.
O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/) e o projeto usa [Versionamento Semântico](https://semver.org/lang/pt-BR/).

## [2.0.0] - Não lançado

### ⚠️ Mudanças que quebram compatibilidade
- O plugin agora suporta **apenas Paper 26.2** e exige **Java 25**. A linha 1.x (Paper 1.21.4, Java 21) não recebe mais suporte.
- O pacote Java foi renomeado de `com.displayah` para `io.github.henrique02w.displayauctionhouse`. Isso só afeta quem dependia das classes do plugin como API; o nome do plugin e os arquivos de dados não mudaram.

### Corrigido
- **Perda de itens ao reiniciar:** os itens eram salvos em um formato que não podia ser lido de volta, então listagens e caixas de entrada podiam ser descartadas ao carregar. Agora usam a serialização nativa do Paper (formato `v2`), que preserva metadados e atualiza os itens entre versões do jogo. Itens no formato antigo são recuperados com tipo e quantidade (metadados não eram guardados), e um backup do arquivo é criado se algum item não puder ser lido.
- **Caixa de entrada:** os itens que o jogador não pegava ao fechar a caixa eram perdidos. Agora voltam para ela ao fechar e no desligamento do servidor.
- **Vault/economia:** o plugin procurava o provedor de economia uma única vez ao iniciar e se desativava se não o encontrasse (plugins de economia costumam ativar depois). Agora o provedor é resolvido sob demanda, o plugin não se desativa e o console informa o estado da economia. Sem economia, a compra avisa o jogador.
- **IDs de display:** remover um display podia fazer o próximo reutilizar o ID de outro e sobrescrevê-lo.
- **Expiração:** a verificação rodava em thread assíncrona mexendo em dados da thread principal. Agora roda na thread principal, a cada minuto, mesmo com `listing-duration-hours: 0`.
- **Rotação:** o giro do item parava para sempre em displays cujo chunk era descarregado.

### Alterado
- Rotação dos itens agora usa uma única tarefa com interpolação de `ItemDisplay`: giro mais suave e muito menos pacotes de rede.
- O tempo restante nas placas é atualizado a cada minuto.
- `paper-api` atualizado para `26.2.build.121-stable`; `api-version` do `plugin.yml` passou para `26.2`.
- VaultAPI passou a vir do JitPack (o repositório anterior foi desativado).
- Autor e site no `plugin.yml`; `dah.admin` passou a incluir `dah.sell`, `dah.buy` e `dah.inbox`.
- `README.md` reescrito e `README.en.md` adicionado.

### Removido
- `maven-shade-plugin` (não havia dependências para embutir) e código não utilizado.

### Infraestrutura
- GitHub Actions: build a cada push/PR e release automática ao enviar uma tag `vX.Y.Z`, com o JAR anexado.
- Dependabot para dependências Maven e Actions; `maven-enforcer-plugin` garantindo JDK 25+.
- `.gitignore`, `CHANGELOG.md` e `CONTRIBUTING.md`.

## [1.0.0]

- Versão inicial para Paper 1.21.4: displays físicos, compra e venda com taxa, expiração, caixa de entrada e integração com Vault.
