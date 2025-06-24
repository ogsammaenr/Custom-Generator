# 🛠️ CustomGenerator

A fully‑featured, BentoBox‑compatible generator plugin designed to provide island owners with the ability to buy, manage, and activate custom cobblestone, stone, basalt, or deepslate generators. All configurations are handled via easy-to-edit YAML files, making customization straightforward and flexible for server administrators.

---

## ✨ Features

* Per‑island **buy & activate** workflow with built-in economy and island-level validations  
* Intuitive **GUI menus** for browsing categories & individual generator types  
* Fully customizable messages, GUI layouts, and generator settings via **YAML files**  
* Efficient **cache-first** design for fast response times and minimal server load  
* Convenient **reload** command to apply configuration changes instantly without server restarts  
* Supports multiple generator categories, including classic cobblestone and advanced basalt and deepslate types  
* Clear separation of concerns with modular code architecture for easier future expansions  
* Economy integration through Vault for seamless transaction management  
* BentoBox island level checks to maintain balanced gameplay progression  

---

## 📦 Hard Dependencies

| Plugin       | Tested Version   | Purpose                              |
|--------------|------------------|-------------------------------------|
| **BentoBox** | 3.0.0+          | Manages island data and world separation |
| **Vault**    | 1.7+             | Provides economy API for transactions |

> The plugin requires both dependencies to be installed and enabled; it will not load otherwise.

---

## 🚀 Quick Start

1. **Download** the `CustomGenerator.jar` file and place it inside your server’s `plugins/` directory.  
2. Start the server once to generate default configuration files:  
   * `messages.yml` — customizable message strings   
   * `generator-types.yml` — definitions for all generator types  
3. Modify the YAML files according to your server’s needs and preferences.  
4. Use the command `/generator reload` or restart the server to load your custom settings.  
5. Encourage players to use `/generator help` to see available commands and usage.  

---

### Commands

| Command | Description |
|---------|-------------|
| `/generator` | Opens the main menu and displays available generators to the player. |
| `/generator buy <generator>` | Purchases the specified generator. The player must have enough money. |
| `/generator activate <generator>` | Activates a previously purchased generator. |
| `/generator list` | Lists all generators defined on the server. |
| `/generator reload` | Reloads all configuration files. Admin permission required. |

## ⚙️ Configuration Basics

### Defining Generator Types
Generator types are defined inside the `generator-types.yml` file. You can add new types or modify existing ones. Each entry specifies how the generator behaves and appears in the GUI. Here is an example:


```
generator-types:
  diamond:
    display-name: "&bDiamond Generator"
    material: DIAMOND_ORE           # GUI icon material (must be a valid Bukkit Material)
    lore:
      - "&bContains rare and valuable resources."
      - "&7Produces 30% diamond and 70% stone blocks."
    generator-type: COBBLESTONE     # Category: one of COBBLESTONE, STONE, BASALT, DEEPSLATE
    price: 5000                     # Cost in economy currency (Vault)
    required-island-level: 30       # Minimum BentoBox island level required
    blocks:                         # Defines block output chances as relative weights (does not need to sum to 1)
      STONE: 70
      DIAMOND_ORE: 30
```
### Important notes:

* The generator-type field must be one of the four hard-coded categories.

* Values under blocks: act as relative weights. The plugin normalizes these internally, so whether you use 70/30, 0.7/0.3, or 7/3, the behavior is consistent.

* The price supports decimal values if your economy plugin allows it.

* material must be a valid [Bukkit Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) for GUI icons.

* lore supports multiple lines and color codes to describe the generator.
