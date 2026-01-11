## Exercice 1 — Analyse de l’application
### Tâche 1 — Ségrégation des responsabilités

#### 1) Quels sont les principaux domaines métiers de l'application Order flow ?

L’application Order Flow repose sur un domaine global appelé Online Shopping Domain qui permet à des clients de réaliser des achats en ligne et ce domaine est découpé en plusieurs sous-domaines:

a) Les core domains sont :
    - le Shopping Cart Domain, responsable de la gestion du panier d’achat,
    - le Order Processing Domain, responsable du passage et du suivi des commandes.

b) Les domaines de support sont :
    - le Product Registry Domain, chargé de la gestion globale des produits,
    - le Product Catalog Domain, chargé de la gestion des produits disponibles à la vente,
    - le Stock Domain, chargé de la gestion des stocks et des réservations,
    - le Customer Domain, chargé de la gestion des comptes clients,
    - le Customer Notification Domain, chargé de la notification des clients.
    
c) il y a egalement des domaines génériques tels que :
    - le Notification Domain,
    - le Billing Domain.

Cette séparation permet d’isoler clairement les responsabilités métiers et de faire évoluer chaque domaine indépendamment.

#### 2) Comment les services sont-ils conçus pour implémenter les domaines métiers ?
L’implémentation suit une architecture orientée services : les composants exécutables sont regroupés dans apps/ et correspondent aux services applicatifs.
Les éléments transverses et partagés sont regroupés dans libs/ (noyau métier, support CQRS, etc...).

Les domaines métiers sont découpés en services spécialisés, avec une séparation CQRS entre écriture (service "domain/command") et lecture (service "read").
Par exemple, le domaine Product Registry est porté par apps/product-registry-domain-service (écriture) et appsproduct-registry-read-service (lecture).

#### 3) Responsabilités des modules
- apps/store-back : BFF qui expose l’API pour le front et appelle les services internes via les contrats (libs:contracts:product-registry-contract), sans porter le domaine.
- apps/store-front : application Angular qui lance le serveur de dev (ng serve) et consomme l’API du BFF.
- libs/kernel : noyau métier partagé utilisé par les services, avec un lien vers le support CQRS (libs:cqrs-support).
- apps/product-registry-domain-service : microservice d’écriture du registre produit , basé sur libs:kernel + libs:cqrs-support et le contrat product-registry-contract.
- apps/product-registry-read-service : microservice de lecture du registre produit basé sur libs:kernel + libs:cqrs-support et le contrat product-registry-contract.
- libs/bom-platform : module BOM qui centralise les versions/dépendances communes du projet.
- libs/cqrs-support : bibliothèque technique CQRS/événements/projections
- libs/sql : changelogs Liquibase et structure SQL de la base (migrations)







### Tâche 2 — Identifier les concepts principaux

#### 1) Concepts principaux utilisés dans l’application Order Flow

Les données métiers sont stockées dans une base de données relationnelle PostgreSQL. Les schémas sont séparés selon les responsabilités, avec notamment des schémas dédiés au domaine, à l’event log, à l’outbox et aux vues de lecture.

Les transactions sont gérées localement à chaque service. Lorsqu’une commande est traitée, la modification de l’état métier et l’écriture de l’événement correspondant sont réalisées dans la même transaction.

Les événements métiers sont conservés dans un journal d’événements. Ils sont ensuite consommés pour mettre à jour les projections de lecture ou pour être diffusés à d’autres services. L’utilisation de l’outbox permet de fiabiliser la publication des événements et d’éviter les pertes en cas de panne.

Les échanges entre services se font principalement via des appels REST et par la propagation d’événements.

La gestion des erreurs distingue les erreurs métier des erreurs techniques. Les erreurs métier sont gérées au niveau du domaine, tandis que les erreurs techniques sont prises en charge par les couches applicatives et d’infrastructure.


#### 2) Implémentation des concepts principaux dans les modules

Les concepts DDD sont implémentés principalement dans la bibliothèque `libs/kernel`. Ce noyau contient les concepts centraux, les règles métier et les structures garantissant la cohérence des états internes.

Le modèle CQRS et la gestion des événements sont implémentés dans la bibliothèque `libs/cqrs-support`. Cette bibliothèque fournit le nécessaire pour la gestion des commandes et des événements. Les services métiers s’appuient sur cette bibliothèque afin de ne pas réimplémenter ces mécanismes pour chaque domaine.

Les services applicatifs, situés dans le dossier `apps/`, utilisent ces bibliothèques pour implémenter leurs responsabilités métier. Les services d’écriture appliquent les règles métier et produisent des événements, tandis que les services de lecture consomment ces événements pour maintenir des vues optimisées pour les requêtes.

Les parties mobiles de l’application correspondent principalement aux modules applicatifs et aux structures de projection, qui peuvent évoluer indépendamment du noyau métier et du support technique CQRS.


#### 3) Rôle de la bibliothèque `libs/cqrs-support`

La bibliothèque `libs/cqrs-support` fournit l’infrastructure technique nécessaire à la mise en œuvre du modèle CQRS et de l’architecture événementielle. Elle gère la définition et la manipulation des événements, la logique de projection, l’accès au journal d’événements et l’intégration du pattern outbox.
Elle est utilisée par les services métiers pour relier la logique métier du noyau aux mécanismes techniques de persistance et de diffusion des événements. 


#### 4) Rôle de la bibliothèque `libs/bom-platform`

La bibliothèque `libs/bom-platform` centralise la définition des versions et des dépendances communes à l’ensemble des modules du projet. Cette approche permet d’assurer une cohérence globale des versions utilisées, de limiter les conflits de dépendances et de faciliter la maintenance et l’évolution du projet.


#### 5) Fiabilité des états internes via le CQRS et le Kernel

La fiabilité des états internes est assurée par la combinaison du noyau et du modèle CQRS. Le noyau garantit le respect des invariants et des règles métier lors du traitement des commandes.

Le modèle CQRS permet de séparer les préoccupations de modification et de consultation de l’état, ce qui réduit la complexité et les risques d’incohérence. Les événements servent de source de vérité pour reconstruire ou projeter l’état, tandis que les vues de lecture sont mises à jour de manière contrôlée et asynchrone.

Cette architecture permet d’obtenir une cohérence forte entre les différents composants du système, tout en conservant une bonne traçabilité des changements.


### Tâche 3 — Identification des problèmes de qualité

#### Méthode utilisée

Afin d’identifier les problèmes de qualité j'ai utitlisé megalinter via son image Docker.

Les linters de type **REPOSITORY** (sécurité, secrets, analyse d’images, dépendances, etc.) ont volontairement été exclus de l’analyse comme indiqué sur le sujet. L’analyse se concentre donc uniquement sur la qualité du code et des fichiers de configuration.


#### Résumé global des problèmes détectés

L’exécution de MegaLinter met en évidence un nombre important de problèmes de qualité, répartis sur plusieurs catégories de fichiers et de technologies.

Les principaux constats sont les suivants :

- Un volume très élevé de problèmes de style et de conventions dans le code Java, détectés par Checkstyle.
- Des duplications de code détectées dans plusieurs parties du projet.
- Des problèmes de validation et de formatage dans les fichiers YAML, HTML, CSS et SQL.
- Quelques problèmes isolés dans le code TypeScript.

#### Problèmes Java (Checkstyle)

Le linter Checkstyle signale un nombre très important d’erreurs dans les modules Java. Ces problèmes concernent principalement :
- le non-respect des conventions de nommage
- la structure des classes et méthodes
- le formatage et l’organisation du code

Ce type de problème nuit à la lisibilité du code, complique la maintenance et rend la collaboration plus difficile dans un contexte de travail en équipe.


#### Duplication de code (Copy/Paste Detection)

Le linter de détection de duplication (jscpd) met en évidence plusieurs blocs de code dupliqués. Ces duplications augmentent le risque d’incohérence lors des évolutions futures, car une modification doit être répercutée à plusieurs endroits du code.


#### Documentation et fichiers de configuration

Des problèmes sont également détectés dans :
- les fichiers YAML (indentation, formatage)
- les fichiers HTML et CSS (conventions et style)
- un fichier SQL présentant des écarts de syntaxe ou de style

Ces problèmes n’empêchent pas le fonctionnement de l’application, mais dégradent la qualité globale du dépôt et la clarté de la documentation.


#### Code TypeScript

Le linter TypeScript détecte un nombre limité de problèmes. Ceux-ci sont mineurs mais indiquent néanmoins des écarts par rapport aux bonnes pratiques de typage et de structuration du code front-end.

