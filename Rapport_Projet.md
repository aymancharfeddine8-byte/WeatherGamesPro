# Rapport de Projet : Application Multi-Services (Météo & Jeux)

**Nom du Projet :** Weather & Games Pro  
**Plateforme :** Android (Java)  
**Architecture :** Activity-based avec Custom Views et intégration d'API REST.

---

## 1. Introduction

Ce projet consiste en une application Android polyvalente combinant un service météorologique en temps réel et une suite de mini-jeux interactifs. L'objectif était de créer une expérience utilisateur fluide tout en maîtrisant des concepts avancés tels que la gestion réseau, le dessin personnalisé sur Canvas, et la génération procédurale.

---

## 2. Module Météo (Fonctionnalités Principales)
Ce module utilise l'API **OpenWeatherMap** pour fournir des données précises.

*   **Recherche de Ville :** Permet à l'utilisateur de consulter la météo pour n'importe quelle ville mondiale.
*   **Affichage Dynamique :** Présentation de la température, de l'humidité, et de la vitesse du vent sous forme de cartes élégantes (CardView).
*   **Prévisions à 5 jours :** Un écran dédié affiche les prévisions futures via un `RecyclerView` horizontal.
*   **Alertes de Sécurité :** Système de notifications automatiques qui avertit l'utilisateur en cas de conditions météorologiques dangereuses (orages, neige, etc.).

---

## 3. Suite de Jeux "Bad Weather? No Problem!"
Conçu pour divertir l'utilisateur lorsque les conditions extérieures sont mauvaises, ce module propose trois expériences distinctes :

### A. Le Labyrinthe Hanté (Haunted Dark Maze)
Un jeu de réflexion et de survie basé sur une vue personnalisée (`MazeView`).
*   **Génération Procédurale :** Le labyrinthe (grille de 25x40) est généré aléatoirement via un algorithme de *backtracking*.
*   **Effet de Torche :** L'écran est plongé dans l'obscurité. L'utilisateur doit déplacer son doigt pour éclairer le chemin.
*   **Ennemis (Fantômes) :** Des fantômes rouges errent dans le noir. Le joueur doit les éviter.
*   **Système de Trace :** Une traînée cyan marque le chemin parcouru pour aider à l'orientation.

### B. Atelier de Dessin (Drawing Pad)
*   **Défis Secrets :** L'application propose des tâches de dessin cachées.
*   **Outils Avancés :** Taille du pinceau, opacité et palette de couleurs complète.
*   **Effets Spéciaux :** Modes "Glow" (néon) et "Rainbow".
*   **Partage :** Exportation et partage des créations.

### C. Stand de Tir (Shooting Target)
*   **Physique de Tir :** Un viseur oscillant simule la difficulté de viser.
*   **Feedback Immersif :** Flash de tir, tremblement de la caméra et vibrations.
*   **Système de Score :** Multiplicateurs de points selon la précision (Bullseye, Great, Hit).

---

## 4. Aspects Techniques
*   **Gestion Réseau :** Utilisation de **Volley** pour des requêtes API asynchrones.
*   **Graphismes :** Utilisation de la classe **Canvas** et des **PorterDuffXfermodes** pour les effets de lumière.
*   **Stockage :** Utilisation de **SharedPreferences** pour la persistance des meilleurs scores.

---

## 5. Conclusion
Ce projet démontre une capacité à intégrer des services externes complexes tout en développant des composants UI sur mesure. La diversité des modules assure une maîtrise globale du SDK Android.
