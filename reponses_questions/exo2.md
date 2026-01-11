### Exercice 2 — Tâche 1 : Compléter les commentaires et la Javadoc

Pour cette tâche, j’ai complété la Javadoc manquante dans le module `apps/product-registry-domain-service`.

La recherche initiale de commentaires `TODO: Complete Javadoc` ne retournait plus de résultats dans ce module. J’ai donc utilisé les rapports MegaLinter (Checkstyle) pour identifier les classes et méthodes publiques concernées par des règles de type `MissingJavadocMethod`. Pour chaque élément signalé, j’ai ajouté une Javadoc.

Enfin, j’ai validé que le module compilait et passait ses vérifications via Gradle (`:apps:product-registry-domain-service:check`).
