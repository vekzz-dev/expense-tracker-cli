package io.vekzz_dev.expense_tracker;

import io.vekzz_dev.expense_tracker.persistence.db.DatabaseSetup;
import io.vekzz_dev.expense_tracker.persistence.transaction.TransactionManager;

public class Main {

    public static void main(String[] args) {
        DatabaseSetup.initialize();

        TransactionManager tx = new TransactionManager();
    }
}