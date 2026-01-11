package org.ormi.priv.tfa.orderflow.productregistry;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Point d'entree Quarkus pour le service de registre de produits.
 */

@QuarkusMain
public final class Main {

    private Main() {
    }

    /**
     * Demarre l'application Quarkus.
     *
     * @param args arguments de ligne de commande
     */
    public static void main(final String... args) {
        Quarkus.run(
            ProductRegistryDomainApplication.class,
            (exitCode, exception) -> { },
            args
        );
    }

    public static class ProductRegistryDomainApplication
        implements QuarkusApplication {

        /**
         * Lance l'application et attend l'arret.
         *
         * @param args arguments de ligne de commande
         * @return code de sortie
         * @throws Exception si une erreur survient au demarrage
         */
        @Override
        public int run(final String... args) throws Exception {
            Quarkus.waitForExit();
            return 0;
        }
    }
}
