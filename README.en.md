# 🏛️ DisplayAuctionHouse

[![Build](https://github.com/Henrique02W/DisplayAuctionHouse/actions/workflows/build.yml/badge.svg)](https://github.com/Henrique02W/DisplayAuctionHouse/actions/workflows/build.yml)
[![Release](https://img.shields.io/github/v/release/Henrique02W/DisplayAuctionHouse?display_name=tag)](https://github.com/Henrique02W/DisplayAuctionHouse/releases/latest)
![Minecraft](https://img.shields.io/badge/minecraft-26.2-brightgreen)
![Paper](https://img.shields.io/badge/paper-26.2-blue)
![Java](https://img.shields.io/badge/java-25-orange)
![License](https://img.shields.io/badge/license-Non--Commercial-blue)
![Vault](https://img.shields.io/badge/economy-Vault-yellow)

[🇧🇷 Português](README.md) · 🇺🇸 English

> 🏛️ A different kind of Auction House: no listing menu, just **physical pedestals in the world**.

---

## 📖 About

**DisplayAuctionHouse** brings a more immersive Auction House: instead of a menu, items for sale **float and spin above pedestals in the world**, with a sign showing the item, price, seller and time left.

Design goals:

* Immersive (the market physically exists on your server)
* Fully configurable through `config.yml`
* Easy to administer, with economy through Vault

> The default in-game messages are in Brazilian Portuguese. All of them are editable in `config.yml` under `messages`.

---

## 🛠️ Features

* 🗿 **Physical displays**: pedestals with the item floating and spinning smoothly (`ItemDisplay` with interpolation)
* 🪧 **Info sign**: item, price, seller and remaining time, refreshed every minute
* 💰 **Buying and selling** with a configurable sales tax and a confirmation screen
* ⏳ **Automatic expiration** (in hours, or never)
* 📦 **Inbox**: purchased, expired or removed items are delivered even with a full inventory, and whatever you leave behind goes back to the inbox
* 🔢 **Configurable limits**: min/max price and max listings per player
* 🚫 You cannot buy your own item
* 🎨 Configurable look (pedestal and sign material, rotation speed, item height and scale)
* 💵 **Vault** economy integration

---

## 🚀 Getting started

### Requirements

* **Paper 26.2** server
* **Java 25** or newer
* **Vault** and a compatible economy plugin (e.g. EssentialsX)

> Starting with **2.0.0** the plugin only supports Minecraft 26.2 (Paper). The 1.x line, built for 1.21.4, is no longer supported.

### Installation

1. Download the latest `.jar` from [Releases](https://github.com/Henrique02W/DisplayAuctionHouse/releases/latest)
2. Drop it into `plugins/` together with Vault and an economy plugin
3. Start the server and tweak `plugins/DisplayAuctionHouse/config.yml`
4. Run `/dah reload` to apply changes

### Upgrading from 1.x (1.21.4)

* Back up `plugins/DisplayAuctionHouse/` (especially `listings.yml`, `inboxes.yml` and `displays.yml`).
* The plugin name and data files are unchanged, so existing displays, listings and inboxes should still be recognised. Test on a copy of your server first.
* **Items saved by 1.x**: the old version stored items in a format that did not keep metadata. On load, the plugin recovers each item's type and amount, but names, enchantments and similar data **cannot be recovered**. Items saved by 2.x keep everything. If any item cannot be read, the plugin backs the file up (`*.yml.bak-<timestamp>`) before overwriting it.
* Update the server to Paper 26.2 on Java 25 and replace the jar with the 2.x version.

### Building from source

Requires **JDK 25** and Maven.

```
git clone https://github.com/Henrique02W/DisplayAuctionHouse.git
cd DisplayAuctionHouse
mvn package
```

The jar is written to `target/DisplayAuctionHouse-<version>.jar`.

---

## 🔑 Configuration

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

| Field | Description |
|---|---|
| `tax-rate` | Tax taken from each sale (0.0 = none, 0.1 = 10%). |
| `listing-duration-hours` | Hours until an item expires (0 = never). |
| `min-price` / `max-price` | Allowed listing price range. |
| `max-listings-per-player` | Max simultaneous listings per player. |
| `display.pedestal-material` | Block used as the pedestal. |
| `display.sign-material` | Wall sign showing the item info. |
| `display.rotation-speed` | Rotation speed in degrees per tick (0 = still). |
| `display.item-height` | Item height above the pedestal, in blocks. |
| `display.item-scale` | Visual scale of the item. |

---

## 🧠 Usage

The main command is `/dah` (aliases: `/ah`, `/auctionhouse`).

| Action | How |
|---|---|
| List an item | **Shift + right-click** an empty display while holding the item, then type the price in chat |
| Buy | **Right-click** an occupied display and confirm in the menu |
| Remove your own listing | **Shift + right-click** the display of your item |
| Open the inbox | `/dah inbox` |
| See your listings | `/dah listings` |
| Remove a listing | `/dah remove <id>` |

Admin commands (`dah.admin`): `/dah adddisplay` (creates a display at your position), `/dah removedisplay <id>`, `/dah listdisplays` and `/dah reload`. Displays and their signs are protected from breaking and editing: remove them only with the command.

### Permissions

| Permission | Description | Default |
|---|---|---|
| `dah.sell` | Sell items | `true` |
| `dah.buy` | Buy items | `true` |
| `dah.inbox` | Use the inbox | `true` |
| `dah.admin` | Manage displays, reload, remove other players' listings | `op` |

---

## 🔒 License

This project is under the **Custom Non-Commercial Software License v1.0** (see [`LICENSE.md`](LICENSE.md); a Portuguese version is in [`LICENSE_pt.md`](LICENSE_pt.md), and the English version prevails in case of conflict).

⚠️ **Commercial use is strictly prohibited.** You may use it personally or for education, and fork and modify it. You may not sell the plugin or monetize any part of the project. For commercial use, contact the author.

## 🤝 Contributing & issues

See [`CONTRIBUTING.md`](CONTRIBUTING.md). Found a bug or have an idea? Open an [issue](https://github.com/Henrique02W/DisplayAuctionHouse/issues) with your Paper and plugin versions and logs if possible.

## 📬 Contact

👤 **Henrique02W** — GitHub: <https://github.com/Henrique02W> · Discord: henrique02#7075
