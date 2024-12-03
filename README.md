# YanQue

A lightweight distributed database management system developed in pure Java.

## Introduction

- **Flexibility:** The program is developed in pure Java, allowing users who are proficient in Java to freely refactor, trim, and update the system to meet the specific needs of their projects.
- **Lightweight:** The system does not require a powerful CPU or large amounts of memory during operation, making it suitable for projects of various types and sizes that require distributed databases.
- **Deadlock Avoidance:** The system uses a non-preemptive method to lock resources needed for transaction execution, avoiding distributed deadlocks and ensuring the orderly progression of transactions, thereby achieving serializability of distributed transactions.
- **ACID:**
  - **Atomicity:** The submission of distributed transactions is confirmed by the global database management system. Only when the global database management system confirms that the transaction can be committed, the local transaction manager is allowed to actually commit the transaction, ensuring the atomicity of distributed transactions.
  - **Consistency:** In distributed transactions, the commit operation is recorded by the global database management system to ensure that modifications to all databases are synchronized. If a local database encounters an exception, it must be recovered through the global database management system. This mechanism ensures that the distributed database transitions from one consistent state to a new consistent state.
  - **Isolation:** The execution of distributed transactions relies on distributed locks to ensure isolation during transaction execution. Combined with the isolation settings provided by the underlying database, developers can customize the transaction isolation level according to specific needs.
  - **Durability:** All updates of a transaction are written to the underlying database systems, such as MySQL and SQL Server, to ensure data persistence after the transaction is completed.

## Features

- **Framework:** The system consists of a Global Database Management System (GDBMS) and a Local Database Management System (LDBMS). The code structure of the two subsystems is shown in Figure 1.
<div align = center><img src=".\assets\2024-12-03_111552.jpg" style="zoom:50%;" /><p> Figure 1</p></div>
- **System Code:** Responsible for maintaining system operation and providing the APIs needed for users to execute transactions.

- **Client Code:** Responsible for specifying the resources and executing SQL statements required for transactions to complete the business processing needed by the project. Client code can be hardcoded into the system code or dynamically loaded and hot-swapped. For dynamic loading, the JLauncher program is used to dynamically start Java programs. JLauncher will also be open-sourced later, or other hot-plug program components can be used.

## Join the Project

  The system currently only has a basic version of the code and has not been tested.

  If you are interested in this project or want to learn more details, please feel free to contact me. I look forward to collaborating with you to create amazing results!

**Contact:**

Email: [qh156256356@outlook.com](mailto:qh156256356@outlook.com)