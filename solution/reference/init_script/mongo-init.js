print("Start creating user for databases")

db = db.getSiblingDB('reference');
db.createUser(
    {
        user: 'default',
        pwd: 'default',
        roles: [{role: 'readWrite', db: 'reference'}]
    }
);
