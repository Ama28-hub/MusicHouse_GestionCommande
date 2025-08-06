# Music House – Gestion de commandes

Une application client-serveur de gestion des commandes pour un site de vente d’instruments de musique, réalisée en Java sous Apache NetBeans dans le cadre du projet Système de Gestion de Base de Données.

---

## Table des matières

- [Description](#description)  
- [Objectifs](#objectifs)  
- [Technologies](#technologies)  
- [Installation](#installation)  
- [Configuration de la base de données](#configuration-de-la-base-de-données)  
- [Lancement de l’application](#lancement-de-lapplication)  
- [Fonctionnalités principales](#fonctionnalités-principales)  
- [Modélisation et scripts](#modélisation-et-scripts)  
- [Procédures et triggers](#procédures-et-triggers)  
- [Licence](#licence)  
- [Auteur](#auteur)  

---

## Description

Music House permet d’enregistrer, suivre et exécuter les commandes d’instruments de musique par des clients. L’application couvre la connexion et la gestion des droits utilisateur, le CRUD via le pattern MVC, et génère automatiquement des factures au format PDF :contentReference[oaicite:4]{index=4}.

## Objectifs

- Optimiser les procédures de traitement des commandes  
- Faciliter le suivi en temps réel des commandes  
- Améliorer la planification des livraisons  
- Appliquer les concepts UML (MCD/MPD), MVC, triggers et procédures stockées :contentReference[oaicite:5]{index=5}

## Technologies

- **Langage** : Java 11+  
- **IDE** : Apache NetBeans  
- **Base de données** : MySQL (script de création avec triggers et procédures)  
- **Pattern** : MVC  
- **PDF** : iText ou bibliothèque équivalente  
- **Fichier de config** : connexion chiffrée  

## Installation

1. **Cloner le dépôt :**
   ```bash
   git clone https://github.com/Ama28-hub/MusicHouse.git
   cd MusicHouse
