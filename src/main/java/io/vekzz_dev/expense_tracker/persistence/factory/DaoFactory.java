package io.vekzz_dev.expense_tracker.persistence.factory;

import io.vekzz_dev.expense_tracker.persistence.dao.ExpenseDao;

public interface DaoFactory {

    //Multiples DAOs
    ExpenseDao expenseDao();
}
