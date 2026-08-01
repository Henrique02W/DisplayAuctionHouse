# 🏛️ DisplayAuctionHouse

![Licença](https://img.shields.io/badge/licença-Non--Commercial-blue)
![Status](https://img.shields.io/badge/status-ativo-success)
![Versão](https://img.shields.io/badge/versão-1.0.0-informational)
![Java](https://img.shields.io/badge/java-21-orange)
![Minecraft](https://img.shields.io/badge/minecraft-1.21.4-brightgreen)
![Vault](https://img.shields.io/badge/economia-Vault-yellow)
![Contribuições](https://img.shields.io/badge/contribuições-bem--vindas-orange)

> 🏛️ Um Auction House diferente: sem GUI, com **pedestais físicos no mundo**.

---

## 📖 Sobre o Projeto

O **DisplayAuctionHouse** foi desenvolvido para trazer uma experiência de Auction House mais imersiva: em vez de um menu (GUI), os itens à venda ficam **flutuando fisicamente sobre pedestais no mundo**, com uma placa exibindo vendedor, item, preço e tempo de expiração.

O projeto foi pensado para ser:

* Imersivo (o mercado existe fisicamente no seu servidor)
* Totalmente configurável via `config.yml`
* Simples de administrar, com economia integrada via Vault

---

## 🛠️ Funcionalidades

* 🗿 **Displays físicos**: pedestais no mundo com item flutuante rotacionando sobre eles
* 🪧 **Placa informativa**: mostra vendedor, item, preço e tempo restante de expiração
* 💰 **Sistema de compra/venda** com taxa configurável sobre cada venda
* ⏳ **Expiração automática** de listagens (configurável em horas, ou nunca expira)
* 📦 **Caixa de entrada (inbox)**: itens comprados ou expirados são entregues automaticamente, mesmo com inventário cheio
* 🔢 **Limites configuráveis**: preço mínimo/máximo por listagem e número máximo de listagens simultâneas por jogador
* 🚫 Proteção contra compra do próprio item listado
* 🎨 Visual do display totalmente customizável (material do pedestal, material da placa, velocidade de rotação, altura e escala do item)
* 💵 Integração com **Vault** para economia

---

## 🧰 Tecnologias Utilizadas

* ☕ Java 21
* 📦 Maven (com shading de dependências)
* 🧾 Vault API (economia)
* 🗺️ Minecraft 1.21.4 (Paper)

---

## 📂 Estrutura do Projeto

```bash
DisplayAuctionHouse/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/displayah/
│       │       └── DisplayAuctionHouse.java
│       └── resources/
│           ├── config.yml
│           └── plugin.yml
├── pom.xml
├── LICENSE.md
├── LICENSE_pt.md
└── README.md
```

---

## 🚀 Começando

### 📦 Requisitos

* Servidor Paper para Minecraft 1.21.4
* Java 21 ou superior
* Vault instalado, com um plugin de economia compatível (ex: EssentialsX Economy)

---

### ⚙️ Instalação

```bash
# Clonar o repositório
git clone https://github.com/Henrique02W/DisplayAuctionHouse.git

# Entrar na pasta
cd DisplayAuctionHouse

# Compilar com Maven
mvn clean package
```

Pegue o `.jar` gerado em `target/` e coloque na pasta `/plugins` do seu servidor, junto com o Vault e um plugin de economia.

---

### 🔑 Configuração

Edite o arquivo `config.yml` gerado na pasta do plugin:

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

#### Principais campos:

| Campo | Descrição |
|---|---|
| `tax-rate` | Taxa cobrada sobre cada venda (0.0 = sem taxa, 0.1 = 10%). |
| `listing-duration-hours` | Tempo em horas até o item expirar no display (0 = nunca expira). |
| `min-price` / `max-price` | Faixa de preço permitida para listagem. |
| `max-listings-per-player` | Máximo de itens que um jogador pode ter listados simultaneamente. |
| `display.pedestal-material` | Bloco usado como pedestal físico. |
| `display.sign-material` | Placa (wall sign) usada para exibir as informações do item. |
| `display.rotation-speed` | Velocidade de rotação do item flutuante (graus por tick, 0 = parado). |
| `display.item-height` | Altura do item acima do topo do pedestal (em blocos). |
| `display.item-scale` | Escala visual do item exibido. |

Todas as mensagens do plugin também são customizáveis em `messages`, incluindo o texto exibido ao interagir com um display.

---

### ▶️ Executando

1. Inicie o servidor com o DisplayAuctionHouse e o Vault instalados
2. Coloque um pedestal em uma área do mundo destinada ao mercado
3. Jogadores podem clicar com shift+direito em um display vazio para listar um item

---

## 🧠 Uso

* `/dah` (ou `/ah`, `/auctionhouse`) — comando principal do plugin
* **Shift + clique direito** em um display vazio: lista o item na mão à venda
* **Clique direito** em um display ocupado: compra o item listado
* **Shift + clique direito** em um item próprio listado: remove a listagem e devolve o item
* Itens comprados, expirados, ou removidos são entregues via **caixa de entrada** (inbox) do jogador

---

## 🔒 Licença

Este projeto está sob uma **Licença Personalizada Não Comercial**.

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

Contribuições são bem-vindas!

## 🐛 Problemas (Issues)

Encontrou um bug ou tem uma sugestão?

* Abra uma issue
* Descreva o problema claramente
* Envie logs ou prints, se possível

---

## 📬 Contato

👤 **Henrique02W**

* GitHub: https://github.com/Henrique02W
* Discord: henrique02#7075

---

## ⭐ Apoie o Projeto

Se você gostou:

* ⭐ Dê uma estrela no repositório
* 🍴 Faça um fork
* 📢 Compartilhe com outras pessoas

---

> “Seu mercado, exposto de verdade no seu mundo.”
