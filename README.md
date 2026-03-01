# Expense Tracker CLI

Track your personal expenses from the terminal. Simple, fast, and local.

This is a challenge from [roadmap.sh](https://roadmap.sh/projects/expense-tracker).

## What You Can Do

- **Add** expenses with description and amount
- **List** expenses with filters (date, month, year, range)
- **View** summaries with statistics (total, average, max, min)
- **Update** and **Delete** expenses
- **Local storage** - SQLite database in ~/.expense_tracker/

## Installation (JBang Recommended)

### Option 1: Install Globally with JBang (Recommended)

```bash
# Install JBang (one-time)
curl -Ls https://sh.jbang.dev | bash

# Install expense-tracker CLI
jbang app install --name expense-tracker --java-options "--enable-native-access=ALL-UNNAMED" https://github.com/vekzz-dev/expense-tracker-cli/releases/download/v1.0.0/expense-tracker-cli-1.0.jar
```

### Option 2: Build from Source

```bash
git clone https://github.com/vekzz-dev/expense-tracker-cli.git
cd expense-tracker-cli
./gradlew installDist
./build/install/expense-tracker-cli/bin/expense-tracker-cli
```

## Usage

### Add expense
```bash
expense-tracker add "Coffee" 5.00
```

### List expenses
```bash
expense-tracker list                    # today's expenses
expense-tracker list -all              # all expenses
expense-tracker list -d 2026-03-01    # by date
expense-tracker list -m 3              # by month (March)
expense-tracker list -y 2026          # by year
expense-tracker list -s 2026-01-01 -f 2026-01-31  # custom range
```

### Show summary
```bash
expense-tracker summary                # current month
expense-tracker summary -all          # all-time summary
expense-tracker summary -d 2026-03-01  # daily summary
expense-tracker summary -m 2026-03    # monthly summary
expense-tracker summary -y 2026       # yearly summary
expense-tracker summary -w 2026-03-01  # last 7 days
```

### Update expense
```bash
expense-tracker update 1 --description "Coffee" --amount 4.50
```

### Delete expense
```bash
expense-tracker delete 1
```

### Help
```bash
expense-tracker --help
```

## Features

- **Simple syntax** - Just description and amount
- **Flexible filters** - By date, month, year, or custom range
- **Statistics** - Total, average, max, min expenses
- **Clean tables** - Formatted ASCII output
- **Local database** - No cloud, no accounts

---

*Requires Java 21 or higher*
