
## Установка

Проект cristalix-npcs необходимо включать в один из своих плагинов.

```groovy
repositories {
    maven {
        url 'https://repo.implario.dev/public'
    }
}

dependencies {
    implementation 'ru.cristalix:npcs-bukkit-api:3.0.0'
}
```


## Использование

```java
Npcs.init(plugin); // В onEnable
Npcs.spawn(Npc.builder()...build()); // Чтобы заспавнить
```
