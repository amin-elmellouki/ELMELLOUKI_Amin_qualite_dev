## Exercice 2 Corriger les problèmes de qualité et introduire des tests (Côté métier du registre de produit [OLTP]) 

### Tâche 1 : Compléter les commentaires et la Javadoc

Pour cette tâche, j’ai complété la Javadoc manquante dans le module `apps/product-registry-domain-service`.

La recherche initiale de commentaires `TODO: Complete Javadoc` ne retournait plus de résultats dans ce module. J’ai donc utilisé les rapports MegaLinter (Checkstyle) pour identifier les classes et méthodes publiques concernées par des règles de type `MissingJavadocMethod`. Pour chaque élément signalé, j’ai ajouté une Javadoc.

Enfin, j’ai validé que le module compilait et passait ses vérifications via Gradle (`:apps:product-registry-domain-service:check`).


### Tâche 5 : Questions

#### 1) Quelle est la différence entre les tests unitaires et les tests d’intégration ?

Les tests unitaires visent à tester une unité de code isolée, généralement une classe ou une méthode, indépendamment de ses dépendances. Ils sont rapides à exécuter et permettent de valider la logique métier interne, par exemple les règles de validation, les transitions d’état ou les exceptions levées.

Les tests d’intégration, quant à eux, vérifient le bon fonctionnement de plusieurs composants ensemble. Ils testent l’intégration entre différentes couches de l’application (ressource REST, services applicatifs, mapping, validation, etc.) et simulent souvent des échanges réels, par exemple via une API HTTP.


#### 2) Est-il pertinent de couvrir 100 % de la base de code par des tests ?

Il n’est pas toujours pertinent ni réaliste de viser une couverture de tests à 100 %. Une couverture élevée ne garantit pas automatiquement la qualité des tests ni l’absence de bugs. Certaines parties du code apportent peu de valeur lorsqu’elles sont testées de manière exhaustive.

L’objectif principal des tests est de sécuriser les règles métiers critiques, les comportements importants et les zones à risque. Il est donc préférable de viser une couverture pertinente et ciblée, en priorisant la logique métier, plutôt qu’un pourcentage maximal de couverture sans réelle valeur ajoutée.


#### 3) Quels avantages apporte une architecture en couches d’oignon dans la couverture des tests ?

L’architecture en couches d’oignon permet de séparer clairement les responsabilités et de placer le domaine métier au centre, indépendant des détails techniques. Cette organisation facilite grandement l’écriture de tests à différents niveaux.

Dans la tâche 3, la classe `Product` située dans le kernel a pu être testée uniquement avec des tests unitaires, sans dépendre d’une base de données, d’un framework web ou de Quarkus. Cela permet des tests simples, rapides et très fiables pour la logique métier.

Les couches externes (application, infrastructure, web) peuvent ensuite être testées via des tests d’intégration, en mockant certaines dépendances si nécessaire. Cette séparation améliore la testabilité globale de l’application et permet d’adapter la stratégie de tests selon la nature de chaque couche.


#### 4) Expliquer la nomenclature des packages infra, application, jpa, web, client, model

- **model** : contient les objets du domaine métier (entités, value objects, agrégats). Cette couche porte la logique métier et ne dépend pas des frameworks techniques.

- **application** : contient les services applicatifs et les cas d’usage. Elle orchestre les actions métier en s’appuyant sur le domaine et définit les points d’entrée logiques de l’application.

- **infra** (infrastructure) : regroupe les implémentations techniques concrètes (accès base de données, messaging, configuration, etc.). Elle dépend des couches internes mais celles-ci n’en dépendent pas.

- **jpa** : sous-couche technique dédiée à la persistance des données via JPA/Hibernate. Elle contient les entités persistées, les repositories et le mapping ORM.

- **web** : couche exposant l’application vers l’extérieur, par exemple via des API REST. Elle contient les ressources REST, les contrôleurs et les DTO liés au transport HTTP.

- **client** : contient les composants permettant d’appeler des services externes (API REST, services tiers). Cette couche encapsule les détails des communications sortantes.

Cette organisation permet de limiter les dépendances, d’améliorer la lisibilité du code et de faciliter les tests et l’évolution de l’application.
