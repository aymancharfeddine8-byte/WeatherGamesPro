# Weather & Games Pro 🌦️🎮

An interactive Android application that combines real-time weather forecasting with a suite of engaging mini-games. Designed to provide utility and entertainment in one seamless experience.

## 🚀 Features

### 1. Weather Forecast 🌤️
*   **Real-time Data:** Fetches current weather conditions (temperature, humidity, wind speed) for any city using the OpenWeatherMap API.
*   **5-Day Forecast:** Detailed daily predictions displayed in a horizontal scrolling view.
*   **Smart Alerts:** Automated notifications that warn users about hazardous weather conditions like storms or heavy snow.

### 2. The Haunted Dark Maze 🔦👻
*   **Procedural Generation:** Every level features a unique, complex maze generated using a backtracking algorithm.
*   **Torch Mechanic:** Navigate through pitch-black corridors using a finger-controlled flashlight.
*   **Ghost AI:** Avoid roaming red ghosts that haunt the maze.
*   **Trail System:** A faint path marks where you've been to help you find the exit.

### 3. Creative Drawing Pad 🎨
*   **Secret Challenges:** Unlock hidden drawing tasks to test your creativity.
*   **Advanced Tools:** Adjustable brush size, opacity, and a full color palette.
*   **Special Effects:** Neon glow and dynamic rainbow brush modes.
*   **Share Function:** Export and share your artwork directly from the app.

### 4. Shooting Target Gallery 🎯
*   **Precision Gameplay:** A 2-player competitive mode with oscillating aim mechanics.
*   **Immersive Effects:** Features muzzle flashes, camera shake, and haptic vibration feedback.
*   **Scoring:** Progressive difficulty with score multipliers for bullseyes.

## 🛠️ Technical Overview
*   **Language:** Java
*   **Networking:** Volley for asynchronous API requests.
*   **Graphics:** Custom Canvas drawing and PorterDuff Xfermodes for lighting effects.
*   **UI/UX:** Material Design components, CardViews, and ConstraintLayouts.
*   **Persistence:** SharedPreferences for saving High Scores.

## 🔐 Configuration API (Important)
Pour des raisons de sécurité, la clé API OpenWeatherMap n’est pas incluse dans ce dépôt.

Pour exécuter l’application :
1. Créer une clé API sur OpenWeatherMap
2. Ajouter votre clé dans le fichier de configuration (ex: `Constants.java` ou équivalent)
3. Recompiler et exécuter l’application

> ⚠️ L’application météo nécessite une clé API valide pour fonctionner correctement.

## 📥 Installation
1. Clone the repository.
2. Open in Android Studio.
3. Add your API key (see section above).
4. Build and run on an emulator or physical device (API 24+).

---
*Created as a project for Android Development.*
