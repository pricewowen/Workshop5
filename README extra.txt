WORKSHOP 5 - EMPLOYEE LOGIN SETUP
---------------------------------

Some seed data contains placeholder password hashes (ex: HASHEDPW_007).
These will NOT work with the BCrypt login system used by the application.

To enable login for the employee "ava.roberts", you must generate a BCrypt
hash for the password and update the database.

Login credentials after setup:
Username: ava.roberts
Password: ava123


STEP 1 - Generate BCrypt Hash
-----------------------------

Option A (recommended)

Run the password hash generator included in the project.

In Eclipse:
1. Navigate to:
   src/main/java/com/sait/workshop05/database/PasswordHashGenerator.java

2. Right click the file

3. Select:
   Run As -> Java Application

4. The console will output a BCrypt hash for the password "ava123".

Copy the generated hash.


STEP 2 - Update the Database
----------------------------

Open MySQL Workbench and run the following SQL:

USE BakeryEcommerce;

UPDATE user
SET userPasswordHash = 'PASTE_HASH_HERE'
WHERE userUsername = 'ava.roberts';


STEP 3 - Verify (optional)
--------------------------

Run:

SELECT userId, userUsername, userRole, userPasswordHash
FROM user
WHERE userUsername = 'ava.roberts';


NOTES
-----

BCrypt hashes are salted. This means the hash value will be different each
time it is generated, even for the same password. This is normal.

After completing these steps you should be able to log into the system with:

Username: ava.roberts
Password: ava123