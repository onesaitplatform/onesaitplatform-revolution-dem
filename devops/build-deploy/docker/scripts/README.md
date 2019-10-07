# Prometheus + Onesait Platform

Instructions for the deployment of Prometheus with the Onesait Platform

## Documentation

All the containers are compatible with Rancher which contain a rancher-compose.yml and docker-compose.yml, being the last one the implementation for the cointainer stack.

### Onesait Platform

### op-deploy

Located in the op-deploy file which contains the following services:

#### schedulerdb

This service uses the schedulerdb image from the onesaitplatform docker hub, which is in charge of the task planification of the Onesait Plaform

#### zeppelin

This service uses the Notebook image from the onesaitplatform docker hub, which is necessary for the Notebook Module

#### configdb

This service uses the configdb image from the onesaitplatform docker hub, which contains a Relational DB (MySQL) for the Onesait Platform configuration data. 

#### configinit

This service uses the configinit image from the onesaitplatform docker hub to initialise the configuration of the Onesait Platform.

##### Environment

```powershell
LOADELASTICDB: 'false' 
LOADMONGODB: 'true'
SERVER_NAME: SERVERNAME
```

##### Links

```powershell
- realtimedb:realtimedb
- configdb:configdb
```

#### iotbrokerservice

This service uses the iotbroker image from the onesaitplatform docker hub, to stablish the iot transactions. 

##### Environment

```powershell
KAFKAENABLED: 'false'
SERVER_NAME: SERVERNAME
```

##### Links

```powershell
- monitoringuiservice:monitoringuiservice
- schedulerdb:schedulerdb
- configdb:configdb
- realtimedb:realtimedb
```

#### apimanagerservice

This service uses the apimanager image from the onesaitplatform docker hub, which is necessary for the API Module 

##### Environment

```powershell
SERVER_NAME: SERVERNAME
```

##### Links

```powershell
- monitoringuiservice:monitoringuiservice
- schedulerdb:schedulerdb
- configdb:configdb
- realtimedb:realtimedb
- cacheservice:cacheservice
```

#### loadbalancerservice

This service uses the nginx image from the onesaitplatform docker hub, which establishes the outside connection and load balancing of the Onesait Platform

##### Volumes

```powershell
- /var/onesaitplatform/webprojects:/usr/local/webprojects:rw
- /var/onesaitplatform/nginx/nginx.conf:/etc/nginx/nginx.conf:rw
- /var/onesaitplatform/nginx/certs/platform.cer:/etc/nginx/ssl/platform.cer:ro
- /var/onesaitplatform/nginx/certs/server.key:/etc/nginx/ssl/server.key:ro
```
##### Ports

```powershell
- 443:443/tcp
```

#### quasar

This service uses the quasar image from the onesaitplatform docker hub, which is the SQL query engine on the realtimedb

##### Links

```powershell
- realtimedb:realtimedb
```


#### monitoringuiservice

This service uses the monitoringui image from the onesaitplatform docker hub, which is necessary for the Monitoring User Interface

##### Links

```powershell
- realtimedb:realtimedb
```

#### dashboardengineservice

This service uses the dashboard image from the onesaitplatform docker hub, which is necessary for the Dashboard Module

##### Environment

```powershell
SERVER_NAME: SERVERNAME
```

##### Links

```powershell
- quasar:quasar
- monitoringuiservice:monitoringuiservice
- configdb:configdb
- realtimedb:realtimedb
```

#### routerservice

This service uses the router image from the onesaitplatform docker hub, which manages the cross connections of the DB

##### Environment

```powershell
SERVER_NAME: SERVERNAME
```

#### cacheservice

This service uses the cacheservice image from the onesaitplatform docker hub, being the cache server

##### Environment

```powershell
SERVER_NAME: SERVERNAME
```

##### Links

Links used to connect to the containers needed

```powershell
- controlpanelservice:controlpanelservice
```
#### controlpanelservice

This service uses the controlpanel image from the onesaitplatform docker hub, which is necessary for the Control panel

##### Environment

```powershell
CAPTCHA_ON: 'false'
KAFKAENABLED: 'false'
SERVER_NAME:SERVERNAME
```
##### Environment

```powershell
SERVER_NAME: SERVERNAME
```

#### realtimedb

This service uses the realtimedb image from the onesaitplatform docker hub, which is a Non SQL DB used for all the real time data

Prometheus
---------------------------------------------------------------------------------------------------------------------------------------------

Located in the prometheus file which contains all the necessary services for the Prometheus configuration with Rancher Server

This docker compose belongs to the Catalog that you can find on Rancher, therefore there are no further explanations. 