# 🏛️ DisplayAuctionHouse

[![Build](https://github.com/Henrique02W/DisplayAuctionHouse/actions/workflows/build.yml/badge.svg)](https://github.com/Henrique02W/DisplayAuctionHouse/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Henrique02W/DisplayAuctionHouse?display_name=tag)](https://github.com/Henrique02W/DisplayAuctionHouse/releases/latest)
![Minecraft](https://img.shields.io/badge/minecraft-26.2-brightgreen)
![Paper](https://img.shields.io/badge/paper-26.2-blue)
![Java](https://img.shields.io/badge/java-25-orange)
![Licença](https://img.shields.io/badge/licen%C3%A7a-Non--Commercial-blue)
![Vault](https://img.shields.io/badge/economia-Vault-yellow)

🇧🇷 Português · [🇺🇸 English](README.en.md)

> 🏛️ Um Auction House diferente: sem menu de listagem, com **pedestais físicos no mundo**.

---

## 📖 Sobre o Projeto

O **DisplayAuctionHouse** traz uma experiência de Auction House mais imersiva: em vez de um menu, os itens à venda ficam **flutuando e girando sobre pedestais no mundo**, com uma placa exibindo o item, o preço, o vendedor e o tempo de expiração.

O projeto foi pensado para ser:

* Imersivo (o mercado existe fisicamente no seu servidor)
* Totalmente configurável via `config.yml`
* Simples de administrar, com economia integrada via Vault

---

## 🛠️ Funcionalidades

* 🗿 **Displays físicos**: pedestais no mundo com o item flutuando e girando suavemente (`ItemDisplay` com interpolação)
* 🪧 **Placa informativa**: item, preço, vendedor e tempo restante, atualizado a cada minuto
* 💰 **Compra e venda** com taxa configurável sobre cada venda e tela de confirmação
* ⏳ **Expiração automática** das listagens (em horas, ou nunca expira)
* 📦 **Caixa de entrada (inbox)**: itens comprados, expirados ou removidos chegam mesmo com o inventário cheio, e o que você não pegar volta para a caixa
* 🔢 **Limites configuráveis**: preço mínimo/máximo e número máximo de listagens por jogador
* 🚫 Proteção contra compra do próprio item
* 🎨 Visual configurável (material do pedestal e da placa, velocidade de rotação, altura e escala do item)
* 💵 Integração com **Vault** para economia

---

## 🧰 Tecnologias Utilizadas

* ☕ Java 25
* 📦 Maven
* 🗺️ Paper 26.2
* 🧾 Vault API (economia)

---

## 🚀 Começando

### 📦 Requisitos

* Servidor **Paper 26.2**
* **Java 25** ou superior
* **Vault** e um plugin de economia compatível (ex.: EssentialsX)

> ℹ️ A partir da versão **2.0.0** o plugin suporta apenas o Minecraft 26.2 (Paper). A linha 1.x, feita para o 1.21.4, não recebe mais suporte.

### ⬇️ Instalação

1. Baixe o `.jar` mais recente em [Releases](https://github.com/Henrique02W/DisplayAuctionHouse/releases/latest)
2. Coloque o arquivo na pasta `plugins/` junto com o Vault e o plugin de economia
3. Inicie o servidor e ajuste `plugins/DisplayAuctionHouse/config.yml`
4. Use `/dah reload` para aplicar mudanças

### 🔄 Atualizando da versão 1.x (1.21.4)

* Faça backup da pasta `plugins/DisplayAuctionHouse/` (principalmente `listings.yml`, `inboxes.yml` e `displays.yml`).
* O nome do plugin e os arquivos de dados não mudaram, então displays, listagens e caixas de entrada existentes devem continuar sendo reconhecidos. Teste com uma cópia do servidor antes de atualizar o de produção.
* **Itens salvos pela 1.x**: a versão antiga guardava os itens em um formato que não preservava metadados. Ao carregar, o plugin recupera o tipo e a quantidade de cada item, mas nome, encantamentos e afins **não podem ser recuperados**. Itens salvos pela 2.x preservam tudo. Se algum item não puder ser lido, o plugin cria um backup do arquivo (`*.yml.bak-<data>`) antes de sobrescrevê-lo.
* Atualize o servidor para Paper 26.2 com Java 25 e troque o `.jar` pela versão 2.x.

### 🔨 Compilando a partir do código

Requer **JDK 25** e Maven.

```
git clone https://github.com/Henrique02W/DisplayAuctionHouse.git
cd DisplayAuctionHouse
mvn package
```

O jar será gerado em `target/DisplayAuctionHouse-<versão>.jar`.

---

## 🔑 Configuração

Edite `config.yml`:

```yaml
tax-rate: 0.05
listing-duration-hours: 48
min-price: 1.0
max-price: 10000000.0
max-listings-per-player: 5

display:
  pedestal-material: QUARTZ_PILLAR
  sign-material: SPRUCE_WALL_SIGN
  rotation-speed: 1.5
  item-height: 0.3
  item-scale: 0.3
```

| Campo | Descrição |
|---|---|
| `tax-rate` | Taxa cobrada sobre cada venda (0.0 = sem taxa, 0.1 = 10%). |
| `listing-duration-hours` | Horas até o item expirar no display (0 = nunca expira). |
| `min-price` / `max-price` | Faixa de preço permitida para listagem. |
| `max-listings-per-player` | Máximo de itens listados por jogador ao mesmo tempo. |
| `display.pedestal-material` | Bloco usado como pedestal. |
| `display.sign-material` | Placa (wall sign) que exibe as informações. |
| `display.rotation-speed` | Velocidade de rotação em graus por tick (0 = parado). |
| `display.item-height` | Altura do item acima do pedestal, em blocos. |
| `display.item-scale` | Escala visual do item. |

Todas as mensagens ficam em `messages` (incluindo o texto exibido ao interagir com um display) e aceitam códigos de cor com `&`.

---

## 🧠 Uso

O comando principal é `/dah` (aliases: `/ah`, `/auctionhouse`).

| Ação | Como |
|---|---|
| Colocar um item à venda | **Shift + clique direito** em um display vazio, segurando o item, e digitar o preço no chat |
| Comprar | **Clique direito** em um display ocupado e confirmar no menu |
| Remover a própria listagem | **Shift + clique direito** no display do seu item |
| Abrir a caixa de entrada | `/dah inbox` |
| Ver suas listagens | `/dah listings` |
| Remover uma listagem | `/dah remove <id>` |

Comandos de administração (`dah.admin`): `/dah adddisplay` (cria um display na sua posição), `/dah removedisplay <id>`, `/dah listdisplays` e `/dah reload`. Displays e suas placas são protegidos contra quebra e edição: remova-os apenas por comando.

### Permissões

| Permissão | Descrição | Padrão |
|---|---|---|
| `dah.sell` | Vender itens | `true` |
| `dah.buy` | Comprar itens | `true` |
| `dah.inbox` | Usar a caixa de entrada | `true` |
| `dah.admin` | Gerenciar displays, recarregar e remover listagens de outros | `op` |

---

## 📂 Estrutura do Projeto

```
DisplayAuctionHouse/
├── .github/
│   ├── workflows/            # CI (build) e release por tag
│   └── dependabot.yml
├── src/main/
│   ├── java/io/github/henrique02w/displayauctionhouse/
│   │   ├── DisplayAuctionHouse.java
│   │   ├── commands/         # /dah
│   │   ├── listeners/        # interação com displays e entrada de jogadores
│   │   ├── managers/         # displays, listagens, caixa de entrada, economia
│   │   ├── models/           # AuctionDisplay, AuctionListing
│   │   └── utils/            # cores, serialização de itens, GUI de compra, entrada de preço
│   └── resources/            # config.yml, plugin.yml
├── pom.xml
├── CHANGELOG.md
├── CONTRIBUTING.md
├── LICENSE.md / LICENSE_pt.md
└── README.md / README.en.md
```

---

## 🔒 Licença

Este projeto está sob a **Custom Non-Commercial Software License v1.0** (texto completo em [`LICENSE.md`](LICENSE.md), com versão em português em [`LICENSE_pt.md`](LICENSE_pt.md)).

⚠️ **Uso comercial é estritamente proibido.**

Você pode:

* Usar para fins pessoais
* Usar para fins educacionais
* Fazer forks e modificar

Você NÃO pode:

* Vender o plugin
* Monetizar qualquer parte do projeto

📩 Para uso comercial, entre em contato com o autor.

---

## 🤝 Contribuindo

Contribuições são bem-vindas! Veja o [`CONTRIBUTING.md`](CONTRIBUTING.md).

## 🐛 Problemas (Issues)

Encontrou um bug ou tem uma sugestão?

* Abra uma [issue](https://github.com/Henrique02W/DisplayAuctionHouse/issues)
* Descreva o problema claramente e informe a versão do Paper e do plugin
* Envie logs ou prints, se possível

---

## 📬 Contato

👤 **Henrique02W**

* GitHub: <https://github.com/Henrique02W>
* Discord: henrique02#7075

---

## ⭐ Apoie o Projeto

Se você gostou:

* ⭐ Dê uma estrela no repositório
* 🍴 Faça um fork
* 📢 Compartilhe com outras pessoas

---

> “Seu mercado, exposto de verdade no seu mundo.”
