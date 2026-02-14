package io.vekzz_dev.expense_tracker.persistence.dao;

import io.vekzz_dev.expense_tracker.models.Expense;

import java.util.List;
import java.util.Optional;

public interface ExpenseDao {

    long insert(Expense expense);

    Optional<Expense> findById(long id);

    List<Expense> findAll();

    boolean update(Expense expense);

    boolean delete(long id);
}
