## Exercice 4 — Projection des événements dans des vues matérialisées

### Tâche 1 — Questions sur la base de code

#### 1) Expliquer le rôle de l’interface Projector dans le système de gestion des événements

L’interface `Projector` définit le contrat permettant de projeter des événements métiers vers des vues. Son rôle est de traduire un événement issu du journal d’événements en une modification d’un état de lecture.

Elle permet d’isoler la logique de projection des événements de l’infrastructure et du stockage, tout en garantissant une cohérence entre les événements produits par le domaine et les vues utilisées pour la lecture.


#### 2) Expliquer le rôle du type générique S dans l’interface Projector

Le type générique `S` représente le type de l’état projeté, c’est-à-dire la vue manipulée par le projecteur. Il correspond au modèle de lecture que l’on souhaite construire ou mettre à jour à partir des événements.

Grâce à ce type générique, l’interface `Projector` peut être utilisée pour différents types de projections (produits, catalogues, stocks, etc.) sans être liée à une implémentation spécifique.


#### 3) Compléter la Javadoc de l’interface Projector en ajoutant la description de S

Exemple de Javadoc complétée :

```java
/**
 * Projecteur d'événements vers une vue matérialisée.
 *
 * @param <S> type de l'état projeté représentant la vue matérialisée
 *            construite ou mise à jour à partir des événements métiers
 */
public interface Projector<S> {
    ...
}
```


### Tâche 2 — Questions concernant l’Outboxing

#### 1) Expliquer le rôle de l’interface `OutboxRepository` dans le système de gestion des événements

L’interface `OutboxRepository` définit le contrat d’accès à la table outbox qui contient des messages d’événements à publier vers l’extérieur du service. Elle permet :
- d’insérer un message d’outbox lors d’une action métier
- de récupérer les messages en attente de publication
- de marquer ces messages comme traités (ou de les supprimer) après publication.

Autrement dit, `OutboxRepository` est la frontière entre la logique événementielle et la base de données, et centralise la gestion du cycle de vie d’un message d’outbox.


#### 2) Expliquer comment l’Outbox Pattern permet de garantir la livraison des événements dans un système distribué

Dans un système distribué, le problème classique est de garantir qu’une modification métier en base et l’envoi d’un événement soient cohérents.

L’Outbox Pattern résout cela en imposant une règle :
- la mutation métier et l’écriture du message d’événement sont faites dans la même transaction locale.
- l’envoi réseau de l’événement est fait après la transaction, par un mécanisme asynchrone.

Conséquence : si la transaction DB commit, alors l’événement est stocké en outbox et sera publié tôt ou tard.

#### 3) Décrire le fonctionnement concret de l’Outbox Pattern dans cette application + diagrammes + interactions transactionnelles

**Ce que l’on peut déduire du projet :**
- Le schéma Liquibase mentionne un schéma `eventing` avec :
  - une table `event_log`
  - une table `outbox`
- Les services d’écriture produisent des événements métiers lors des commandes
- Les vues matérialisées sont mises à jour de manière asynchrone via ces événements.

##### Fonctionnement dans ce contexte (lecture du code + schéma Liquibase)
1) Une requête HTTP arrive sur le Command Service (ex: `POST /products`, `PATCH /products/{id}/name`, `DELETE /products/{id}`).
2) Le service applicatif exécute le cas d’usage : il modifie l’état OLTP (ex: `domain.products`).
3) Dans le même traitement, un événement métier est produit (ex: ProductRegistered / ProductNameUpdated / ProductRetired).
4) Dans la même transaction DB, on persiste :
   - l’événement en `eventing.event_log`
   - un message à publier en `eventing.outbox`
5) Un composant asynchrone lit périodiquement `eventing.outbox`, publie l’événement vers les consommateurs puis marque l’outbox comme traitée (delete/flag).