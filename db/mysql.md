# MySQL

## Administration

### SQL mode

| mode                   | Description                                                  |
| ---------------------- | ------------------------------------------------------------ |
| ONLY_FULL_GROUP_BY     | Reject queries for which the `SELECT` list, `Having` condition, or `ORDER BY` list refer to non-aggregated columns that are neither named in `GROUP BY` clause nor are functionally dependent on `GROUP BY` columns |
| ANSI                   | This mode changes syntax and behavior to confirm more closely to standard SQL |
| STRICT_TRANS_TABLES    | if a value could not be inserted as given into a *transactional* table, *abort* the statement. For a *non-transactional* table, abort the statement if the value occurs in a single-row statement or first row of a multiple-row statement. |
| NO_AUTO_CREATE_USER    | Prevent the `GRANT` statement from automatically creating new user accounts if it would otherwise do so, unless *authentication information*(`IDENTIFIED BY`) is specified. |
| NO_AUTO_VALUE_ON_ZERO  | It affects handling of `AUTO_INCREMENT` columns. Normally, you generate the next sequence number for the column by inserting either `NULL` or `0` into it. `NO_AUTO_VALUE_ON_ZERO` suppresses this behavior for `0` so that only `NULL` generates the next sequence number |
| NO_ENGINE_SUBSTITUTION | Control automatic substitution of the default storage engine when a statement such as `CREATE TABLE` or `ALTER TABLE` specifies <u>a storage engine that is disabled or not compiled in</u>. |



