# Wallets Service

Service to manage player's wallets. Players can recharge their wallets using a credit card and spend that money on the platform (bookings, racket rentals, ...).

I added an extra endpoint that allows the creation of wallets by providing only the initial current balance of the account. This was done just to make it easier to test manually with Postman. I also added a Dockerfile and a docker-compose.yml to prepare the application to be containerized.

Regarding concurrency, since I do not possess a lot of professional experience in this, I decided to keep things simple and use a ReadWriteLock, because the _getWallet()_ method only returns information about the wallet and does not change anything. I thought that even though that information might be outdated, because a thread might recharge or charge the wallet at the same time another thread is getting information about the wallet, there would be no harm done. On the other methods it is important to guarantee that only one thread at a time can make changes to the wallet. For these reasons I decided to use this lock, although it is possible that better solutions could be implemented.

Also, when creating tests for the concurrency, I only created tests for the methods that change the wallet's balance (_rechargeWallet_ and _chargeWallet_) because those are the more critical ones. The way I did the test's logic may be a very simplistic way of testing concurrency, but it is the only way I know how to test it. 


### Setup without Docker:

- Clone/extract project to a folder
- Run the application with:
    - _mvn clean install_
    - _mvn spring-boot:run_
- Package the application with _mvn package_


### Setup with Docker:

- Install Docker on your machine
- Launch Docker
- Run the command _sudo systemctl status docker_ to confirm Docker is running
- Open terminal in the project folder
- Run the command _sudo docker-compose build_
- Run the command _sudo docker-compose up -d_
- The container will be up and the application will be running inside


### Endpoints:

The documentation of this API can be found at _http://localhost:8090/swagger-ui.html_ (**Note: you need to initialize the application to access this link**).
