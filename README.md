# Introduction 
This is the documentation source for the price service applications. All those applications were extracted from cart calculation service also known as pricing engine.

The scope of this project is to receive the skus data of each poc for all available zones in the ABI b2b product. The process is divided in three main applications as shown in the architecture image below and which are described following.

![Screenshot](price_architecture.png)

#1. Relay
The entry application is called relay. It consolidate the contracts the zones uses to send the data from their ERPs, then this data is sent to a RabbitMQ instance to be gradually consumed by the next application.

We use swagger to expose the endpoints and contracts of the services we have available they can be accessed in all four environments of the development process they are presented following. The application can be accessed using basic authentication which has the same user and password which is "relay"

|Swagger URL|Service Base Url|
|---|---|
| [LOCAL](http://localhost:8080/swagger-ui.html)| [LOCAL](http://localhost:8080/) | 
| [DEV](https://b2b-services-dev.westeurope.cloudapp.azure.com/v1/cart-calculation-relay/swagger-ui.html) | [DEV](https://b2b-services-dev.westeurope.cloudapp.azure.com/v1/cart-calculation-relay/)|
| [QA/SIT](https://b2b-services-qa.westeurope.cloudapp.azure.com/v1/cart-calculation-relay/swagger-ui.html)| [QA/SIT](https://b2b-services-qa.westeurope.cloudapp.azure.com/v1/cart-calculation-relay/) |
| [UAT](https://b2b-services-uat.westeurope.cloudapp.azure.com/v1/cart-calculation-relay/swagger-ui.html) | [UAT](https://b2b-services-uat.westeurope.cloudapp.azure.com/v1/cart-calculation-relay/) |
| Not Available | [PROD](https://b2b-services.westeurope.cloudapp.azure.com/v1/cart-calculation-relay/) |

#2. Consumer
The middle application is called consumer. It listen to the RabbitMQ messages coming into queues so that it can gradually consumes them and save them into our configured mongoDB database.

#3. Api
The last application is the Api. This application also have its defined contracts so that the cart-calculation can call it to get sku data when needed.

Here we also use swagger to expose the contracts and services we have in the application, but instead of relay here we don't have basic authentication, the service is accessible only through known sources.

|Swagger URL|Service Base Url|
|---|---|
| [LOCAL](http://localhost:8082/swagger-ui.html)| [LOCAL](http://localhost:8082/) | 
| [DEV](https://b2b-services-dev.westeurope.cloudapp.azure.com/v1/cart-calculator/swagger-ui.html) | [DEV](https://b2b-services-dev.westeurope.cloudapp.azure.com/v1/cart-calculator/)|
| [QA/SIT](https://b2b-services-qa.westeurope.cloudapp.azure.com/v1/cart-calculator/swagger-ui.html)| [QA/SIT](https://b2b-services-qa.westeurope.cloudapp.azure.com/v1/cart-calculator/) |
| [UAT](https://b2b-services-uat.westeurope.cloudapp.azure.com/v1/cart-calculator/swagger-ui.html) | [UAT](https://b2b-services-uat.westeurope.cloudapp.azure.com/v1/cart-calculator/) |
| Not Available | [PROD](https://b2b-services.westeurope.cloudapp.azure.com/v1/cart-calculator/) |

#4. Getting Started
To run the project locally we do have a docker-compose file committed on the root path of the project which downloads and run 2 instances of needed applications we use. They are a RabbitMQ instance and a MongoDB Instance. Run the following command to start those necessary instances.

```docker-compose up```

After that you will be able to run the Consumer application which is responsible by creating the rabbitMQ exchanges, queues and bindings. After that you will be able to start the relay application which expose the incoming data services, as the rabbit configurations are already done, it will successfully post the data in messages to the rabbitMQ instance. The consumer then will read those message and save then to the database so that the data will be available to get api get it.