# 🛠️ CustomGenerator

A fully‑featured, BentoBox‑compatible generator plugin designed to provide island owners with the ability to buy, manage, and activate custom cobblestone, stone, basalt, or deepslate generators. All configurations are handled via easy-to-edit YAML files, making customization straightforward and flexible for server administrators.

---

## ✨ Features

* Per‑island **buy & activate** workflow with built-in economy and island-level validations  
* Intuitive **GUI menus** for browsing categories & individual generator types  
* **Custom Generator Logic** powered by `custom-generator-categories.yml`
* Fully customizable messages, GUI layouts, and generator settings via **YAML files**  
* Efficient **cache-first** design for fast response times and minimal server load  
* Convenient **reload** command to apply configuration changes instantly without server restarts  
* Supports multiple generator categories, including classic cobblestone and advanced basalt and deepslate types  
* Clear separation of concerns with modular code architecture for easier future expansions  
* Economy integration through Vault for seamless transaction management  
* BentoBox island level checks to maintain balanced gameplay progression  

---

## 🔄 What’s New in v1.1.0

### 🧪 New Features

- **Custom Generator Categories** via `custom-generator-categories.yml`:  
  Define your own generator logic using fluid-flow conditions, surrounding blocks, biomes, Y-level limits, and more.

- **Improved Generator Menu UI**:  
  Visual enhancements, better layout, and more intuitive category buttons.

- **Pagination Support in GUI**:  
  The menu now supports unlimited generator types, navigable through **Next** / **Previous** page buttons.

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
   * `custom-generator-categories.yml` — defines generator behavior
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


```yaml
generator-types:
  diamond:
    display-name: "&bDiamond Generator"
    material: DIAMOND_ORE           
    lore:
      - "&bContains rare and valuable resources."
      - "&7Produces 30% diamond and 70% stone blocks."
    generator-type: COBBLESTONE    
    price: 5000                     
    required-island-level: 30       
    blocks:                          
      STONE: 70
      DIAMOND_ORE: 30
```
### Important notes:

* generator-type must either be one of the built-in categories (COBBLESTONE, STONE, BASALT, DEEPSLATE) or a custom category defined in custom-generator-categories.yml.

* Values under blocks: act as relative weights. The plugin normalizes these internally, so whether you use 70/30, 0.7/0.3, or 7/3, the behavior is consistent.

* The price supports decimal values if your economy plugin allows it.

* material must be a valid [Bukkit Material](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html) for GUI icons.

* lore supports multiple lines and color codes to describe the generator.

### Creating Custom Generator

Custom generators are defined in custom-generator-categories.yml. Example:

```yaml
dirt_generator:
  category: DIRTGEN
  display-name: "&2Dirt"
  fluid: LAVA
  to: AIR

  conditions:
    sides: [ DIRT ]
    up: [ STONE ]
    down: [ COARSE_DIRT ]

  y-level:
    min: 10
    max: 64

  biomes:
    - minecraft:plains
    - minecraft:the_void
```

### Explanation:
* **category**: Unique category ID. Must match a generator-type entry in generator-types.yml.

* **fluid**: Triggering fluid block (e.g., LAVA, WATER).

* **to**: Block the fluid flows into (AIR, WATER, POWDER_SNOW etc.).

* **conditions**: Optional block requirements (sides, up, down).

* **y-level**: Optional Y-axis range for the generator to be active.

* **biomes**: Optional biome whitelist for where the generator works.

**With this system, you can implement:**

* Basalt generators restricted to the Nether.

* Gold generators above Y=100.

* Dirt generators with specific surrounding blocks.

* Biome-exclusive generators (like Mushroom Islands only).
