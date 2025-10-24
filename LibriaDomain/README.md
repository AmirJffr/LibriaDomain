# 📚 Libria – API REST (Jakarta EE / Payara / Docker)

## ⚙️ Prérequis

Avant de lancer le projet, assurez-vous d’avoir installé et configuré :

| Outil | Version requise | Vérification |
|-------|------------------|--------------|
| **Java JDK** | 17 | `java -version` |
| **Maven** | 3.8+ | `mvn -version` |
| **Docker Desktop** | 24+ | `docker --version` |
| **Git** | 2.x | `git --version` |

> ⚠️ Payara 6 fonctionne uniquement avec **Java 17**.  
> Si vous utilisez une version plus récente (21 ou 25), le serveur ne démarrera pas correctement.

---

## 🚀 Lancer le projet sur Docker et Payara

### 1️⃣ Aller dans le dossier du projet

```bash
cd ./Library-app
```

---

### 2️⃣ Générer le fichier `.war` avec Maven

```bash
mvn clean package
```

Un fichier `Library-app-1.0-SNAPSHOT.war` sera créé dans le dossier :

```
target/
```

---

### 3️⃣ Lancer Payara via Docker

Toujours depuis le dossier du projet, exécutez :

```bash
docker compose down
docker compose up -d
```

---

### 4️⃣ Consulter les logs

```bash
docker compose logs -f payara
```

Vous devez voir la ligne suivante indiquant que le déploiement est terminé avec succès :

```
Library-app-1.0-SNAPSHOT was successfully deployed
```

---

### 5️⃣ Accéder à l’application

- **Application REST :**  
  👉 [http://localhost:8080/Library-app-1.0-SNAPSHOT/api/library](http://localhost:8080/Library-app-1.0-SNAPSHOT/api/library)

- **Console d’administration Payara :**  
  👉 [http://localhost:4848](http://localhost:4848)  
  *(identifiants par défaut : admin / admin)*

---

### 6️⃣ Tester les endpoints

Afficher tous les livres :
```bash
curl http://localhost:8080/Library-app-1.0-SNAPSHOT/api/library/books
```

Ajouter un livre :
```bash
curl -X POST http://localhost:8080/Library-app-1.0-SNAPSHOT/api/library/books   -H "Content-Type: application/json"   -d '{"isbn":"ISBN-001","title":"Clean Code","genre":"Informatique"}'
```

Afficher un utilisateur :
```bash
curl http://localhost:8080/Library-app-1.0-SNAPSHOT/api/library/users
```

---

### 7️⃣ Arrêter le serveur

Pour arrêter le conteneur Payara :
```bash
docker compose down
```

---

### ✅ Résumé rapide des commandes

```bash
cd ./Library-app
mvn clean package
docker compose down
docker compose up -d
docker compose logs -f payara
```

Application disponible sur :  
👉 [http://localhost:8080/Library-app-1.0-SNAPSHOT/api/library](http://localhost:8080/Library-app-1.0-SNAPSHOT/api/library)
