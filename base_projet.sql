CREATE DATABASE IF NOT EXISTS projet_java;
USE projet_java;

DROP TABLE IF EXISTS Note;
DROP TABLE IF EXISTS Humeur;
DROP TABLE IF EXISTS Utilisateur;

CREATE TABLE utilisateur (
    id_utilisateur INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    mot_de_passe VARCHAR(100) NOT NULL
);

CREATE TABLE Humeur (
    id_humeur INT AUTO_INCREMENT PRIMARY KEY,
    emoji VARCHAR(50),
    nom_humeur VARCHAR(100)
);

CREATE TABLE Note (
    id_note INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(100),
    contenu TEXT,
    humeur VARCHAR(100),
    date_creation DATE,
    id_utilisateur INT,
    id_humeur INT,
    FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE,
    FOREIGN KEY (id_humeur) REFERENCES Humeur(id_humeur) ON DELETE SET NULL
);