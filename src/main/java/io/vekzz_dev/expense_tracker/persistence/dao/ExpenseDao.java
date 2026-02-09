package io.vekzz_dev.expense_tracker.persistence.dao;

import io.vekzz_dev.expense_tracker.models.Expense;

import java.util.List;
import java.util.Optional;

public interface ExpenseDao {

    long save(Expense expense);

    Optional<Expense> findById(long id);

    List<Expense> findAll();

    void update(Expense expense);

    void delete(long id);
}
